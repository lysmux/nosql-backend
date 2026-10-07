package ru.itmo.delivery.menu.internal.adapters.outbound.dao.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "menu_items")
class MenuItemEntity(
    @Id
    @Column(name = "menu_item_id")
    var menuItemId: UUID,
    @Column(name = "restaurant_id")
    var restaurantId: UUID,
    var name: String,
    var description: String?,
    var price: BigDecimal,
    var available: Boolean,
    var archived: Boolean,
    @Column(name = "created_at")
    var createdAt: Instant,
    @Column(name = "updated_at")
    var updatedAt: Instant,
)
