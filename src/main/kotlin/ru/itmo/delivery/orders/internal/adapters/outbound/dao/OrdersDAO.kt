package ru.itmo.delivery.orders.internal.adapters.outbound.dao

import ru.itmo.delivery.orders.api.models.Order
import ru.itmo.delivery.orders.api.models.OrderDetails
import ru.itmo.delivery.orders.api.models.OrderFilter
import ru.itmo.delivery.orders.api.models.OrderScope
import ru.itmo.delivery.orders.api.models.OrderSort
import ru.itmo.delivery.orders.api.models.StatusHistoryEntry
import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

interface OrdersDAO {
    fun save(details: OrderDetails)

    fun findDetails(orderId: UUID): OrderDetails?

    fun applyTransition(order: Order, entry: StatusHistoryEntry): Boolean

    fun assignFreeCourier(couriers: Collection<UUID>): CourierAssignment?

    fun existsActiveByCourier(courierId: UUID): Boolean

    fun findPage(
        scope: OrderScope,
        requesterId: UUID,
        restaurantIds: Collection<UUID>?,
        page: Int,
        size: Int,
        filter: OrderFilter,
        sort: OrderSort,
    ): Paged<Order>
}

data class CourierAssignment(val orderId: UUID, val courierId: UUID)
