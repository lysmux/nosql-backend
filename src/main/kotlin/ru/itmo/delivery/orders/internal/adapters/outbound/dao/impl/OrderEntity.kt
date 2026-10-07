package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import ru.itmo.delivery.orders.api.models.OrderStatus
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @Column(name = "order_id")
    var orderId: UUID,
    @Column(name = "user_id")
    var userId: UUID,
    @Column(name = "restaurant_id")
    var restaurantId: UUID,
    @Column(name = "restaurant_name")
    var restaurantName: String,
    @Column(name = "delivery_address")
    var deliveryAddress: String,
    @Column(name = "total_amount")
    var totalAmount: BigDecimal,
    @Enumerated(EnumType.STRING)
    var status: OrderStatus,
    @Column(name = "courier_id")
    var courierId: UUID?,
    @Column(name = "created_at")
    var createdAt: Instant,
    @Column(name = "updated_at")
    var updatedAt: Instant,
)
