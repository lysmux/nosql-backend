package ru.itmo.delivery.users.internal.adapters.outbound.dao.impl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserJpaRepository : JpaRepository<UserEntity, UUID> {
    @Query("select * from users where lower(email) = :email", nativeQuery = true)
    fun findByEmail(email: String): UserEntity?

    @Query("select count(*) > 0 from users where lower(email) = :email", nativeQuery = true)
    fun existsByEmail(email: String): Boolean
}
