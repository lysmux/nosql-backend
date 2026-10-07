package ru.itmo.delivery.orders.internal.adapters.outbound.dao

import ru.itmo.delivery.orders.api.models.Order
import ru.itmo.delivery.orders.api.models.OrderDetails
import ru.itmo.delivery.orders.api.models.OrderScope
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.orders.api.models.StatusHistoryEntry
import ru.itmo.delivery.shared.models.Paged
import java.time.Instant
import java.util.UUID

interface OrdersDAO {
    fun save(details: OrderDetails)

    fun findDetails(orderId: UUID): OrderDetails?

    fun findDetailsForUpdate(orderId: UUID): OrderDetails?

    fun applyTransition(order: Order, entry: StatusHistoryEntry)

    fun assignFreeCourier(now: Instant): Int

    fun findPage(
        scope: OrderScope,
        requesterId: UUID,
        restaurantId: UUID?,
        status: OrderStatus?,
        page: Int,
        size: Int,
    ): Paged<Order>
}
