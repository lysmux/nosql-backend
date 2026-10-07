package ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface NotificationJpaRepository : JpaRepository<NotificationEntity, UUID> {
    @Query(
        value = """
            select n.* from notifications n
            where n.user_id = :userId
            order by n.created_at desc, n.notification_id desc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun pageByUser(
        @Param("userId") userId: UUID,
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<NotificationEntity>

    @Query(value = "select count(*) from notifications n where n.user_id = :userId", nativeQuery = true)
    fun countByUser(@Param("userId") userId: UUID): Long
}
