package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.api.models.OrderStatus
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "order_status_history")
class OrderStatusHistoryEntity(
    @Id
    @Column(name = "history_id")
    var historyId: UUID,
    @Column(name = "order_id")
    var orderId: UUID,
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    var fromStatus: OrderStatus?,
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status")
    var toStatus: OrderStatus,
    @Enumerated(EnumType.STRING)
    @Column(name = "actor_type")
    var actorType: ActorType,
    @Column(name = "actor_id")
    var actorId: UUID?,
    var reason: String,
    @Column(name = "created_at")
    var createdAt: Instant,
)
