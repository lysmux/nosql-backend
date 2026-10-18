package ru.itmo.delivery.menu.internal.adapters.outbound.dao.impl

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@Document(collection = MenuItemDocument.COLLECTION)
data class MenuItemDocument(
    @Id val id: UUID,
    val restaurantId: UUID,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val available: Boolean,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    companion object {
        const val COLLECTION = "menu_items"
    }
}
