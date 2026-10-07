package ru.itmo.delivery.orders.api.models

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Order(
    val orderId: UUID,
    val userId: UUID,
    val restaurantId: UUID,
    val restaurantName: String,
    val items: List<OrderItem>,
    val deliveryAddress: String,
    val totalAmount: BigDecimal,
    val status: OrderStatus,
    val courierId: UUID?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class OrderItem(
    val menuItemId: UUID,
    val name: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal,
)

data class StatusHistoryEntry(
    val fromStatus: OrderStatus?,
    val toStatus: OrderStatus,
    val actorType: ActorType,
    val actorId: UUID?,
    val reason: String,
    val createdAt: Instant,
)

data class OrderDetails(
    val order: Order,
    val history: List<StatusHistoryEntry>,
)

enum class OrderStatus {
    CREATED,
    COOKING,
    READY,
    IN_DELIVERY,
    DELIVERED,
    CANCELLED,
}

enum class OrderScope {
    CUSTOMER,
    RESTAURANT,
    COURIER,
}

enum class ActorType {
    USER,
    RESTAURANT,
    SYSTEM,
}
