package ru.itmo.delivery.orders.internal

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.itmo.delivery.menu.api.Menu
import ru.itmo.delivery.menu.api.exceptions.MenuItemNotFoundException
import ru.itmo.delivery.menu.api.models.OrderMenuItem
import ru.itmo.delivery.orders.api.Orders
import ru.itmo.delivery.orders.api.events.OrderEventType
import ru.itmo.delivery.orders.api.events.OrderStatusChanged
import ru.itmo.delivery.orders.api.exceptions.OrderItemsConflictException
import ru.itmo.delivery.orders.api.exceptions.OrderNotFoundException
import ru.itmo.delivery.orders.api.exceptions.OrderStatusConflictException
import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.api.models.CreateOrderCommand
import ru.itmo.delivery.orders.api.models.Order
import ru.itmo.delivery.orders.api.models.OrderDetails
import ru.itmo.delivery.orders.api.models.OrderFilter
import ru.itmo.delivery.orders.api.models.OrderItem
import ru.itmo.delivery.orders.api.models.OrderItemRequest
import ru.itmo.delivery.orders.api.models.OrderScope
import ru.itmo.delivery.orders.api.models.OrderSort
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.orders.api.models.StatusHistoryEntry
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.OrdersDAO
import ru.itmo.delivery.restaurants.api.Restaurants
import ru.itmo.delivery.shared.exceptions.ForbiddenException
import ru.itmo.delivery.shared.exceptions.ValidationException
import ru.itmo.delivery.shared.models.Paged
import ru.itmo.delivery.users.api.Users
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class OrdersService(
    private val dao: OrdersDAO,
    private val restaurants: Restaurants,
    private val menu: Menu,
    private val users: Users,
    private val events: ApplicationEventPublisher,
) : Orders {
    @Transactional
    override fun create(userId: UUID, command: CreateOrderCommand): OrderDetails {
        val address = checkAddress(command.deliveryAddress)
        checkRequestedItems(command.items)

        val restaurant = restaurants.get(command.restaurantId)
        val menuItems = menu.lookupForOrder(command.items.map { it.menuItemId })
        val items = snapshotItems(command.restaurantId, command.items, menuItems)

        val timestamp = now()
        val order = Order(
            orderId = UUID.randomUUID(),
            userId = userId,
            restaurantId = restaurant.restaurantId,
            restaurantName = restaurant.name,
            items = items,
            deliveryAddress = address,
            totalAmount = checkTotal(items),
            status = OrderStatus.CREATED,
            courierId = null,
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        val placed = StatusHistoryEntry(
            fromStatus = null,
            toStatus = OrderStatus.CREATED,
            actorType = ActorType.SYSTEM,
            actorId = null,
            reason = ORDER_PLACED_REASON,
            createdAt = timestamp,
        )
        val details = OrderDetails(order, listOf(placed))

        dao.save(details)
        events.publishEvent(OrderStatusChanged(order, OrderEventType.ORDER_CREATED))
        return details
    }

    override fun list(
        requesterId: UUID,
        scope: OrderScope,
        page: Int,
        size: Int,
        filter: OrderFilter,
        sort: OrderSort,
    ): Paged<Order> {
        if (page < 0 || size !in MIN_PAGE_SIZE..MAX_PAGE_SIZE) {
            throw ValidationException("page должен быть >= 0, size от $MIN_PAGE_SIZE до $MAX_PAGE_SIZE")
        }
        if (scope == OrderScope.COURIER && users.get(requesterId)?.courierModeEnabled != true) {
            throw ForbiddenException("Режим курьера не включён")
        }

        val restaurantIds = when (scope) {
            OrderScope.RESTAURANT -> ownedRestaurantIds(requesterId, filter.restaurantId)
            else -> null
        }
        return dao.findPage(scope, requesterId, restaurantIds, page, size, checkFilter(filter), sort)
    }

    private fun checkFilter(filter: OrderFilter): OrderFilter {
        val itemName = filter.itemName?.trim()?.takeIf { it.isNotEmpty() }
        if (itemName != null && itemName.length > MAX_ITEM_NAME_LENGTH) {
            throw ValidationException("itemName должен быть от 1 до $MAX_ITEM_NAME_LENGTH символов")
        }

        return filter.copy(itemName = itemName)
    }

    private fun ownedRestaurantIds(requesterId: UUID, restaurantId: UUID?): List<UUID> =
        if (restaurantId != null) {
            listOf(restaurants.requireOwned(requesterId, restaurantId).restaurantId)
        } else {
            restaurants.ownedIds(requesterId)
        }

    override fun read(requesterId: UUID, orderId: UUID): OrderDetails {
        val details = dao.findDetails(orderId) ?: throw OrderNotFoundException(orderId)
        if (!isParticipant(requesterId, details.order)) throw OrderNotFoundException(orderId)
        return details
    }

    @Transactional
    override fun startPreparation(ownerId: UUID, orderId: UUID) =
        transition(ownerId, orderId, OrderAction.START_PREPARATION)

    @Transactional
    override fun markReady(ownerId: UUID, orderId: UUID) = transition(ownerId, orderId, OrderAction.MARK_READY)

    @Transactional
    override fun pickup(courierId: UUID, orderId: UUID) = transition(courierId, orderId, OrderAction.PICKUP)

    @Transactional
    override fun deliver(courierId: UUID, orderId: UUID) = transition(courierId, orderId, OrderAction.DELIVER)

    @Transactional
    override fun cancel(requesterId: UUID, orderId: UUID) = transition(requesterId, orderId, OrderAction.CANCEL)

    private fun transition(actorId: UUID, orderId: UUID, action: OrderAction): OrderDetails {
        val details = dao.findDetails(orderId) ?: throw OrderNotFoundException(orderId)
        val current = details.order
        val roles = rolesOf(actorId, current)
        if (roles.isEmpty()) throw OrderNotFoundException(orderId)
        val step = OrderTransitions.resolve(action, roles, current.status)

        val timestamp = now()
        val updated = current.copy(
            status = step.to,
            courierId = if (step.to == OrderStatus.CANCELLED) null else current.courierId,
            updatedAt = timestamp,
        )
        val entry = StatusHistoryEntry(
            fromStatus = current.status,
            toStatus = step.to,
            actorType = step.actorType,
            actorId = actorId,
            reason = step.reason,
            createdAt = timestamp,
        )

        if (!dao.applyTransition(updated, entry)) {
            throw OrderStatusConflictException("Заказ изменён параллельно, повторите запрос")
        }
        releaseCourier(current, step.to)
        events.publishEvent(OrderStatusChanged(updated, OrderEventType.ORDER_STATUS_CHANGED))
        return OrderDetails(updated, details.history + entry)
    }

    private fun releaseCourier(order: Order, newStatus: OrderStatus) {
        val courierId = order.courierId ?: return
        if (newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED) users.clearActiveOrder(courierId)
    }

    private fun rolesOf(actorId: UUID, order: Order): Set<OrderRole> = buildSet {
        if (order.userId == actorId) add(OrderRole.CUSTOMER)
        if (order.courierId == actorId) add(OrderRole.COURIER)
        if (restaurants.get(order.restaurantId).ownerId == actorId) add(OrderRole.RESTAURANT_OWNER)
    }

    private fun isParticipant(requesterId: UUID, order: Order): Boolean =
        order.userId == requesterId ||
            order.courierId == requesterId ||
            restaurants.get(order.restaurantId).ownerId == requesterId

    private fun checkAddress(address: String): String {
        val trimmed = address.trim()
        if (trimmed.isEmpty() || trimmed.length > MAX_ADDRESS_LENGTH) {
            throw ValidationException("Адрес доставки должен быть от 1 до $MAX_ADDRESS_LENGTH символов")
        }

        return trimmed
    }

    private fun checkRequestedItems(requested: List<OrderItemRequest>) {
        if (requested.size !in 1..MAX_ITEMS) {
            throw ValidationException("Заказ должен содержать от 1 до $MAX_ITEMS позиций")
        }

        if (requested.any { it.quantity !in 1..MAX_QUANTITY }) {
            throw ValidationException("Количество каждой позиции должно быть от 1 до $MAX_QUANTITY")
        }

        if (requested.map { it.menuItemId }.toSet().size != requested.size) {
            throw ValidationException("Позиции в заказе не должны повторяться")
        }
    }

    private fun snapshotItems(
        restaurantId: UUID,
        requested: List<OrderItemRequest>,
        menuItems: Map<UUID, OrderMenuItem>,
    ): List<OrderItem> {
        val resolved = requested.map { request ->
            request to (menuItems[request.menuItemId] ?: throw MenuItemNotFoundException(request.menuItemId))
        }
        if (resolved.any { (_, menuItem) -> menuItem.restaurantId != restaurantId }) {
            throw ValidationException("Все позиции должны принадлежать одному ресторану")
        }
        if (resolved.any { (_, menuItem) -> menuItem.archived || !menuItem.available }) {
            throw OrderItemsConflictException("Часть позиций недоступна для заказа")
        }

        return resolved.map { (request, menuItem) ->
            OrderItem(
                menuItemId = menuItem.menuItemId,
                name = menuItem.name,
                unitPrice = menuItem.price,
                quantity = request.quantity,
                lineTotal = menuItem.price.multiply(BigDecimal(request.quantity))
                    .setScale(MONEY_SCALE, RoundingMode.HALF_UP),
            )
        }
    }

    private fun checkTotal(items: List<OrderItem>): BigDecimal {
        val zero = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP)
        val total = items.fold(zero) { sum, item -> sum + item.lineTotal }
        if (total > MAX_TOTAL) throw ValidationException("Сумма заказа не должна превышать $MAX_TOTAL")

        return total
    }

    private fun now(): Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

    private companion object {
        const val ORDER_PLACED_REASON = "Заказ оформлен"
        const val MAX_ADDRESS_LENGTH = 500
        const val MAX_ITEM_NAME_LENGTH = 100
        const val MAX_ITEMS = 100
        const val MAX_QUANTITY = 99
        const val MIN_PAGE_SIZE = 1
        const val MAX_PAGE_SIZE = 100
        const val MONEY_SCALE = 2
        val MAX_TOTAL = BigDecimal("999999999.99")
    }
}
