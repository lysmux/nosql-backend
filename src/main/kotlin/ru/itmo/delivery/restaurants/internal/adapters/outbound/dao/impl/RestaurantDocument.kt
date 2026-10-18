package ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.impl

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collection = RestaurantDocument.COLLECTION)
data class RestaurantDocument(
    @Id val id: UUID,
    val ownerId: UUID,
    val name: String,
    val createdAt: Instant,
) {
    companion object {
        const val COLLECTION = "restaurants"
    }
}
