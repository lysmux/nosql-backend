package ru.itmo.delivery.orders.internal.adapters.inbound.rest

import ru.itmo.delivery.entrypoint.api.model.ActorTypeDto
import ru.itmo.delivery.entrypoint.api.model.CreateOrderRequestDto
import ru.itmo.delivery.entrypoint.api.model.OrderDetailsDto
import ru.itmo.delivery.entrypoint.api.model.OrderDto
import ru.itmo.delivery.entrypoint.api.model.OrderItemDto
import ru.itmo.delivery.entrypoint.api.model.OrderPageDto
import ru.itmo.delivery.entrypoint.api.model.OrderScopeDto
import ru.itmo.delivery.entrypoint.api.model.OrderStatusDto
import ru.itmo.delivery.entrypoint.api.model.StatusHistoryEntryDto
import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.api.models.CreateOrderCommand
import ru.itmo.delivery.orders.api.models.Order
import ru.itmo.delivery.orders.api.models.OrderDetails
import ru.itmo.delivery.orders.api.models.OrderItem
import ru.itmo.delivery.orders.api.models.OrderItemRequest
import ru.itmo.delivery.orders.api.models.OrderScope
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.orders.api.models.StatusHistoryEntry
import ru.itmo.delivery.shared.models.Paged

fun CreateOrderRequestDto.toCommand() = CreateOrderCommand(
    restaurantId = restaurantId,
    items = items.map { OrderItemRequest(it.menuItemId, it.quantity) },
    deliveryAddress = deliveryAddress,
)

fun OrderScopeDto.toDomain() = OrderScope.valueOf(name)

fun OrderStatusDto.toDomain() = OrderStatus.valueOf(name)

fun OrderStatus.toDto() = OrderStatusDto.valueOf(name)

fun ActorType.toDto() = ActorTypeDto.valueOf(name)

fun OrderItem.toDto() = OrderItemDto(
    menuItemId = menuItemId,
    name = name,
    unitPrice = unitPrice,
    quantity = quantity,
    lineTotal = lineTotal,
)

fun StatusHistoryEntry.toDto() = StatusHistoryEntryDto(
    fromStatus = fromStatus?.toDto(),
    toStatus = toStatus.toDto(),
    actorType = actorType.toDto(),
    actorId = actorId,
    reason = reason,
    createdAt = createdAt,
)

fun Order.toDto() = OrderDto(
    orderId = orderId,
    userId = userId,
    restaurantId = restaurantId,
    restaurantName = restaurantName,
    items = items.map { it.toDto() },
    deliveryAddress = deliveryAddress,
    totalAmount = totalAmount,
    status = status.toDto(),
    courierId = courierId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun OrderDetails.toDto() = OrderDetailsDto(
    history = history.map { it.toDto() },
    orderId = order.orderId,
    userId = order.userId,
    restaurantId = order.restaurantId,
    restaurantName = order.restaurantName,
    items = order.items.map { it.toDto() },
    deliveryAddress = order.deliveryAddress,
    totalAmount = order.totalAmount,
    status = order.status.toDto(),
    courierId = order.courierId,
    createdAt = order.createdAt,
    updatedAt = order.updatedAt,
)

fun Paged<Order>.toPageDto() = OrderPageDto(
    items = items.map { it.toDto() },
    page = page,
    pageSize = size,
    totalItems = totalItems,
    totalPages = totalPages,
)
