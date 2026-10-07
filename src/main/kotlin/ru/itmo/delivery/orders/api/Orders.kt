package ru.itmo.delivery.orders.api

import ru.itmo.delivery.orders.api.models.CreateOrderCommand
import ru.itmo.delivery.orders.api.models.Order
import ru.itmo.delivery.orders.api.models.OrderDetails
import ru.itmo.delivery.orders.api.models.OrderScope
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

interface Orders {
    fun create(userId: UUID, command: CreateOrderCommand): OrderDetails

    fun list(
        requesterId: UUID,
        scope: OrderScope,
        restaurantId: UUID?,
        status: OrderStatus?,
        page: Int,
        size: Int,
    ): Paged<Order>

    fun read(requesterId: UUID, orderId: UUID): OrderDetails

    fun startPreparation(ownerId: UUID, orderId: UUID): OrderDetails

    fun markReady(ownerId: UUID, orderId: UUID): OrderDetails

    fun pickup(courierId: UUID, orderId: UUID): OrderDetails

    fun deliver(courierId: UUID, orderId: UUID): OrderDetails

    fun cancel(requesterId: UUID, orderId: UUID): OrderDetails
}
