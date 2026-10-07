package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.itmo.delivery.orders.api.models.Order
import ru.itmo.delivery.orders.api.models.OrderDetails
import ru.itmo.delivery.orders.api.models.OrderItem
import ru.itmo.delivery.orders.api.models.OrderScope
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.orders.api.models.StatusHistoryEntry
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.OrdersDAO
import ru.itmo.delivery.shared.models.Paged
import java.time.Instant
import java.util.UUID

@Repository
class OrdersDefaultDao(
    private val orders: OrderJpaRepository,
    private val items: OrderItemJpaRepository,
    private val history: OrderStatusHistoryJpaRepository,
) : OrdersDAO {
    @Transactional
    override fun save(details: OrderDetails) {
        val order = details.order
        orders.save(order.toEntity())
        items.saveAll(order.items.map { it.toEntity(order.orderId) })
        history.saveAll(details.history.map { it.toEntity(order.orderId) })
    }

    @Transactional(readOnly = true)
    override fun findDetails(orderId: UUID): OrderDetails? =
        orders.findById(orderId).orElse(null)?.let { toDetails(it) }

    @Transactional
    override fun findDetailsForUpdate(orderId: UUID): OrderDetails? =
        orders.findByIdForUpdate(orderId)?.let { toDetails(it) }

    @Transactional
    override fun applyTransition(order: Order, entry: StatusHistoryEntry) {
        orders.save(order.toEntity())
        history.save(entry.toEntity(order.orderId))
    }

    @Transactional
    override fun assignFreeCourier(now: Instant): Int = orders.assignFreeCourier(now)

    private fun toDetails(entity: OrderEntity): OrderDetails {
        val orderId = entity.orderId
        val orderItems = items.findAllByOrderIds(listOf(orderId)).map { it.toItem() }
        return OrderDetails(
            order = entity.toOrder(orderItems),
            history = history.findAllByOrderId(orderId).map { it.toEntry() },
        )
    }

    @Transactional(readOnly = true)
    override fun findPage(
        scope: OrderScope,
        requesterId: UUID,
        restaurantId: UUID?,
        status: OrderStatus?,
        page: Int,
        size: Int,
    ): Paged<Order> {
        val statusName = status?.name
        val offset = page.toLong() * size
        val (entities, total) = when {
            scope == OrderScope.CUSTOMER ->
                orders.pageByCustomer(requesterId, statusName, size, offset) to
                    orders.countByCustomer(requesterId, statusName)

            scope == OrderScope.COURIER ->
                orders.pageByCourier(requesterId, statusName, size, offset) to
                    orders.countByCourier(requesterId, statusName)

            restaurantId != null ->
                orders.pageByRestaurant(restaurantId, statusName, size, offset) to
                    orders.countByRestaurant(restaurantId, statusName)

            else ->
                orders.pageByRestaurantOwner(requesterId, statusName, size, offset) to
                    orders.countByRestaurantOwner(requesterId, statusName)
        }
        val itemsByOrder = if (entities.isEmpty()) {
            emptyMap()
        } else {
            items.findAllByOrderIds(entities.map { it.orderId }).groupBy({ it.orderId }, { it.toItem() })
        }
        return Paged(entities.map { it.toOrder(itemsByOrder[it.orderId].orEmpty()) }, page, size, total)
    }

    private fun Order.toEntity() = OrderEntity(
        orderId = orderId,
        userId = userId,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        deliveryAddress = deliveryAddress,
        totalAmount = totalAmount,
        status = status,
        courierId = courierId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun OrderItem.toEntity(orderId: UUID) = OrderItemEntity(
        orderItemId = UUID.randomUUID(),
        orderId = orderId,
        menuItemId = menuItemId,
        name = name,
        unitPrice = unitPrice,
        quantity = quantity,
        lineTotal = lineTotal,
    )

    private fun StatusHistoryEntry.toEntity(orderId: UUID) = OrderStatusHistoryEntity(
        historyId = UUID.randomUUID(),
        orderId = orderId,
        fromStatus = fromStatus,
        toStatus = toStatus,
        actorType = actorType,
        actorId = actorId,
        reason = reason,
        createdAt = createdAt,
    )

    private fun OrderEntity.toOrder(orderItems: List<OrderItem>) = Order(
        orderId = orderId,
        userId = userId,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        items = orderItems,
        deliveryAddress = deliveryAddress,
        totalAmount = totalAmount,
        status = status,
        courierId = courierId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun OrderItemEntity.toItem() = OrderItem(
        menuItemId = menuItemId,
        name = name,
        unitPrice = unitPrice,
        quantity = quantity,
        lineTotal = lineTotal,
    )

    private fun OrderStatusHistoryEntity.toEntry() = StatusHistoryEntry(
        fromStatus = fromStatus,
        toStatus = toStatus,
        actorType = actorType,
        actorId = actorId,
        reason = reason,
        createdAt = createdAt,
    )
}
