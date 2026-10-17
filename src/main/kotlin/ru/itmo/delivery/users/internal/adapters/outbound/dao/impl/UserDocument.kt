package ru.itmo.delivery.users.internal.adapters.outbound.dao.impl

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collection = UserDocument.COLLECTION)
data class UserDocument(
    @Id val id: UUID,
    val name: String,
    val email: String,
    val passwordHash: String,
    val courierModeEnabled: Boolean,
    val createdAt: Instant,
) {
    companion object {
        const val COLLECTION = "users"
    }
}
