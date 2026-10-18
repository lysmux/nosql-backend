package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.api.models.Order
import ru.itmo.delivery.orders.api.models.OrderDetails
import ru.itmo.delivery.orders.api.models.OrderFilter
import ru.itmo.delivery.orders.api.models.OrderItem
import ru.itmo.delivery.orders.api.models.OrderScope
import ru.itmo.delivery.orders.api.models.OrderSort
import ru.itmo.delivery.orders.api.models.OrderSortField
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.orders.api.models.SortDirection
import ru.itmo.delivery.orders.api.models.StatusHistoryEntry
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.CourierAssignment
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.OrdersDAO
import ru.itmo.delivery.shared.models.Paged
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.regex.Pattern

@Repository
class OrdersMongoDao(
    private val template: MongoTemplate,
) : OrdersDAO {
    override fun save(details: OrderDetails) {
        template.insert(details.toDocument())
    }

    override fun findDetails(orderId: UUID): OrderDetails? =
        template.findById(orderId, OrderDocument::class.java)?.toDetails()

    override fun applyTransition(order: Order, entry: StatusHistoryEntry): Boolean {
        val unchanged = Query(
            Criteria.where("_id").`is`(order.orderId)
                .and("status").`is`(entry.fromStatus),
        )

        val update = Update()
            .set("status", order.status)
            .set("courierId", order.courierId)
            .set("updatedAt", order.updatedAt)
            .push("history", entry.toDocument())

        return template.updateFirst(unchanged, update, OrderDocument::class.java).matchedCount > 0
    }

    override fun existsActiveByCourier(courierId: UUID): Boolean =
        template.exists(
            Query(
                Criteria.where("courierId").`is`(courierId)
                    .and("status").`in`(ACTIVE_STATUSES),
            ),
            OrderDocument::class.java,
        )

    override fun assignFreeCourier(couriers: Collection<UUID>): CourierAssignment? {
        val oldestUnassigned = Query(
            Criteria.where("status").`is`(OrderStatus.READY)
                .and("courierId").isNull(),
        ).with(Sort.by(Sort.Direction.ASC, "createdAt", "_id"))
        val picked = template.findOne(oldestUnassigned, OrderDocument::class.java) ?: return null

        val busy = template.findDistinct(
            Query(
                Criteria.where("status").`in`(ACTIVE_STATUSES)
                    .and("courierId").ne(null),
            ),
            "courierId",
            OrderDocument::class.java,
            UUID::class.java,
        )
        val courier = (couriers - busy.toSet()).randomOrNull() ?: return null

        val stillUnassigned = Query(
            Criteria.where("_id").`is`(picked.id)
                .and("status").`is`(OrderStatus.READY)
                .and("courierId").isNull(),
        )

        val update = Update()
            .set("courierId", courier)
            .set("updatedAt", Instant.now().truncatedTo(ChronoUnit.MILLIS))
        val matched = template.updateFirst(stillUnassigned, update, OrderDocument::class.java)
            .matchedCount > 0
        return if (matched) CourierAssignment(picked.id, courier) else null
    }

    override fun findPage(
        scope: OrderScope,
        requesterId: UUID,
        restaurantIds: Collection<UUID>?,
        page: Int,
        size: Int,
        filter: OrderFilter,
        sort: OrderSort,
    ): Paged<Order> {
        val conditions = mutableListOf(
            when (scope) {
                OrderScope.CUSTOMER -> Criteria.where("userId").`is`(requesterId)

                OrderScope.COURIER -> Criteria.where("courierId").`is`(requesterId)

                OrderScope.RESTAURANT -> {
                    if (restaurantIds.isNullOrEmpty()) return Paged(emptyList(), page, size, 0)
                    Criteria.where("restaurantId").`in`(restaurantIds)
                }
            },
        )
        filter.status?.let { conditions += Criteria.where("status").`is`(it) }
        itemCondition(filter)?.let { conditions += Criteria.where("items").elemMatch(it) }
        filter.cancelledBy?.let { conditions += cancelledByCondition(it) }

        val query = Query(Criteria().andOperator(conditions))
        val total = template.count(query, OrderDocument::class.java)

        query.with(sort.toMongoSort()).skip(page.toLong() * size).limit(size)
        val orders = template.find(query, OrderDocument::class.java).map { it.toOrder() }
        return Paged(orders, page, size, total)
    }

    private fun itemCondition(filter: OrderFilter): Criteria? {
        val byName = filter.itemName?.let { Criteria.where("name").regex(Pattern.quote(it), "i") }
        val byMenuItem = filter.menuItemId?.let { Criteria.where("menuItemId").`is`(it) }

        return when {
            byName != null && byMenuItem != null -> Criteria().andOperator(byName, byMenuItem)
            else -> byName ?: byMenuItem
        }
    }

    private fun cancelledByCondition(actor: ActorType) =
        Criteria.where("history").elemMatch(
            Criteria.where("status").`is`(OrderStatus.CANCELLED)
                .and("actorType").`is`(actor),
        )

    private fun OrderSort.toMongoSort(): Sort {
        val direction = if (this.direction == SortDirection.ASC) Sort.Direction.ASC else Sort.Direction.DESC
        val property = when (field) {
            OrderSortField.CREATED_AT -> "createdAt"
            OrderSortField.UPDATED_AT -> "updatedAt"
            OrderSortField.TOTAL_AMOUNT -> "totalAmount"
            OrderSortField.STATUS -> "status"
        }

        return Sort.by(direction, property, "_id")
    }

    private fun OrderDetails.toDocument() = OrderDocument(
        id = order.orderId,
        userId = order.userId,
        restaurantId = order.restaurantId,
        restaurantName = order.restaurantName,
        items = order.items.map { it.toDocument() },
        deliveryAddress = order.deliveryAddress,
        totalAmount = order.totalAmount,
        status = order.status,
        courierId = order.courierId,
        history = history.map { it.toDocument() },
        createdAt = order.createdAt,
        updatedAt = order.updatedAt,
    )

    private fun OrderItem.toDocument() = OrderItemDocument(
        menuItemId = menuItemId,
        name = name,
        unitPrice = unitPrice,
        quantity = quantity,
        lineTotal = lineTotal,
    )

    private fun StatusHistoryEntry.toDocument() = StatusHistoryDocument(
        status = toStatus,
        actorType = actorType,
        actorId = actorId,
        reason = reason,
        createdAt = createdAt,
    )

    private fun OrderDocument.toDetails() = OrderDetails(
        order = toOrder(),
        history = history.mapIndexed { index, document ->
            document.toEntry(fromStatus = history.getOrNull(index - 1)?.status)
        },
    )

    private fun OrderDocument.toOrder() = Order(
        orderId = id,
        userId = userId,
        restaurantId = restaurantId,
        restaurantName = restaurantName,
        items = items.map { it.toItem() },
        deliveryAddress = deliveryAddress,
        totalAmount = totalAmount,
        status = status,
        courierId = courierId,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun OrderItemDocument.toItem() = OrderItem(
        menuItemId = menuItemId,
        name = name,
        unitPrice = unitPrice,
        quantity = quantity,
        lineTotal = lineTotal,
    )

    private fun StatusHistoryDocument.toEntry(fromStatus: OrderStatus?) = StatusHistoryEntry(
        fromStatus = fromStatus,
        toStatus = status,
        actorType = actorType,
        actorId = actorId,
        reason = reason,
        createdAt = createdAt,
    )

    private companion object {
        val ACTIVE_STATUSES = listOf(OrderStatus.READY, OrderStatus.IN_DELIVERY)
    }
}
