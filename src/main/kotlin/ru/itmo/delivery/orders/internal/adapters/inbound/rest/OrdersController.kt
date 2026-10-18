package ru.itmo.delivery.orders.internal.adapters.inbound.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import ru.itmo.delivery.auth.api.currentUserId
import ru.itmo.delivery.entrypoint.api.OrdersApi
import ru.itmo.delivery.entrypoint.api.model.ActorTypeDto
import ru.itmo.delivery.entrypoint.api.model.CreateOrderRequestDto
import ru.itmo.delivery.entrypoint.api.model.OrderDetailsDto
import ru.itmo.delivery.entrypoint.api.model.OrderPageDto
import ru.itmo.delivery.entrypoint.api.model.OrderScopeDto
import ru.itmo.delivery.entrypoint.api.model.OrderSortFieldDto
import ru.itmo.delivery.entrypoint.api.model.OrderStatusDto
import ru.itmo.delivery.entrypoint.api.model.SortDirectionDto
import ru.itmo.delivery.orders.api.Orders
import ru.itmo.delivery.orders.api.models.OrderFilter
import java.util.UUID

@RestController
class OrdersController(
    private val orders: Orders,
) : OrdersApi {
    override fun ordersCreate(createOrderRequestDto: CreateOrderRequestDto): ResponseEntity<OrderDetailsDto> =
        ResponseEntity.status(HttpStatus.CREATED)
            .body(orders.create(currentUserId(), createOrderRequestDto.toCommand()).toDto())

    override fun ordersList(
        scope: OrderScopeDto,
        restaurantId: UUID?,
        status: OrderStatusDto?,
        itemName: String?,
        menuItemId: UUID?,
        cancelledBy: ActorTypeDto?,
        sortBy: OrderSortFieldDto?,
        sortDirection: SortDirectionDto?,
        page: Int,
        size: Int,
    ): ResponseEntity<OrderPageDto> =
        ResponseEntity.ok(
            orders.list(
                requesterId = currentUserId(),
                scope = scope.toDomain(),
                page = page,
                size = size,
                filter = OrderFilter(
                    restaurantId = restaurantId,
                    status = status?.toDomain(),
                    itemName = itemName,
                    menuItemId = menuItemId,
                    cancelledBy = cancelledBy?.toDomain(),
                ),
                sort = orderSort(sortBy, sortDirection),
            ).toPageDto(),
        )

    override fun ordersRead(orderId: UUID): ResponseEntity<OrderDetailsDto> =
        ResponseEntity.ok(orders.read(currentUserId(), orderId).toDto())

    override fun ordersStartPreparation(orderId: UUID): ResponseEntity<OrderDetailsDto> =
        ResponseEntity.ok(orders.startPreparation(currentUserId(), orderId).toDto())

    override fun ordersMarkReady(orderId: UUID): ResponseEntity<OrderDetailsDto> =
        ResponseEntity.ok(orders.markReady(currentUserId(), orderId).toDto())

    override fun ordersPickup(orderId: UUID): ResponseEntity<OrderDetailsDto> =
        ResponseEntity.ok(orders.pickup(currentUserId(), orderId).toDto())

    override fun ordersDeliver(orderId: UUID): ResponseEntity<OrderDetailsDto> =
        ResponseEntity.ok(orders.deliver(currentUserId(), orderId).toDto())

    override fun ordersCancel(orderId: UUID): ResponseEntity<OrderDetailsDto> =
        ResponseEntity.ok(orders.cancel(currentUserId(), orderId).toDto())
}
