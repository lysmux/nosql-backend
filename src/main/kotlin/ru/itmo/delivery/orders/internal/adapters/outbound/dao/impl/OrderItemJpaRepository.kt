package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface OrderItemJpaRepository : JpaRepository<OrderItemEntity, UUID> {
    @Query(
        value = """
            select * from order_items
            where order_id in (:orderIds)
            order by order_id asc, name asc, order_item_id asc
        """,
        nativeQuery = true,
    )
    fun findAllByOrderIds(@Param("orderIds") orderIds: Collection<UUID>): List<OrderItemEntity>
}
