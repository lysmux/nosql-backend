package ru.itmo.delivery.users.internal.adapters.outbound.dao.impl

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @Column(name = "user_id")
    var userId: UUID,
    var name: String,
    var email: String,
    @Column(name = "password_hash")
    var passwordHash: String,
    @Column(name = "courier_mode_enabled")
    var courierModeEnabled: Boolean,
    @Column(name = "created_at")
    var createdAt: Instant,
)
