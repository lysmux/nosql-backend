package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface OrderStatusHistoryJpaRepository : JpaRepository<OrderStatusHistoryEntity, UUID> {
    @Query(
        value = """
            select * from order_status_history
            where order_id = :orderId
            order by created_at asc, history_id asc
        """,
        nativeQuery = true,
    )
    fun findAllByOrderId(@Param("orderId") orderId: UUID): List<OrderStatusHistoryEntity>
}
