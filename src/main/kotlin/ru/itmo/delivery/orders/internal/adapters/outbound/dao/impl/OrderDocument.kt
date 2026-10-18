package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.api.models.OrderStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Document(collection = OrderDocument.COLLECTION)
data class OrderDocument(
    @Id val id: UUID,
    val userId: UUID,
    val restaurantId: UUID,
    val restaurantName: String,
    val items: List<OrderItemDocument>,
    val deliveryAddress: String,
    val totalAmount: BigDecimal,
    val status: OrderStatus,
    val courierId: UUID?,
    val history: List<StatusHistoryDocument>,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        const val COLLECTION = "orders"
    }
}

data class OrderItemDocument(
    val menuItemId: UUID,
    val name: String,
    val unitPrice: BigDecimal,
    val quantity: Int,
    val lineTotal: BigDecimal,
)

data class StatusHistoryDocument(
    val status: OrderStatus,
    val actorType: ActorType,
    val actorId: UUID?,
    val reason: String,
    val createdAt: Instant,
)
