package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(name = "order_items")
class OrderItemEntity(
    @Id
    @Column(name = "order_item_id")
    var orderItemId: UUID,
    @Column(name = "order_id")
    var orderId: UUID,
    @Column(name = "menu_item_id")
    var menuItemId: UUID,
    var name: String,
    @Column(name = "unit_price")
    var unitPrice: BigDecimal,
    var quantity: Int,
    @Column(name = "line_total")
    var lineTotal: BigDecimal,
)
