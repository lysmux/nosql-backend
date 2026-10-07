package ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "restaurants")
class RestaurantEntity(
    @Id
    @Column(name = "restaurant_id")
    var restaurantId: UUID,
    @Column(name = "owner_id")
    var ownerId: UUID,
    var name: String,
    @Column(name = "created_at")
    var createdAt: Instant,
)
