package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface OrderJpaRepository : JpaRepository<OrderEntity, UUID> {
    @Query(
        value = """
            select o.* from orders o
            where o.user_id = :userId
              and (cast(:status as varchar) is null or o.status = cast(:status as varchar))
            order by o.created_at desc, o.order_id desc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun pageByCustomer(
        @Param("userId") userId: UUID,
        @Param("status") status: String?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<OrderEntity>

    @Query(
        value = """
            select count(*) from orders o
            where o.user_id = :userId
              and (cast(:status as varchar) is null or o.status = cast(:status as varchar))
        """,
        nativeQuery = true,
    )
    fun countByCustomer(@Param("userId") userId: UUID, @Param("status") status: String?): Long

    @Query(
        value = """
            select o.* from orders o
            where o.restaurant_id = :restaurantId
              and (cast(:status as varchar) is null or o.status = cast(:status as varchar))
            order by o.created_at desc, o.order_id desc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun pageByRestaurant(
        @Param("restaurantId") restaurantId: UUID,
        @Param("status") status: String?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<OrderEntity>

    @Query(
        value = """
            select count(*) from orders o
            where o.restaurant_id = :restaurantId
              and (cast(:status as varchar) is null or o.status = cast(:status as varchar))
        """,
        nativeQuery = true,
    )
    fun countByRestaurant(@Param("restaurantId") restaurantId: UUID, @Param("status") status: String?): Long

    @Query(
        value = """
            select o.* from orders o
            join restaurants r on r.restaurant_id = o.restaurant_id
            where r.owner_id = :ownerId
              and (cast(:status as varchar) is null or o.status = cast(:status as varchar))
            order by o.created_at desc, o.order_id desc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun pageByRestaurantOwner(
        @Param("ownerId") ownerId: UUID,
        @Param("status") status: String?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<OrderEntity>

    @Query(
        value = """
            select count(*) from orders o
            join restaurants r on r.restaurant_id = o.restaurant_id
            where r.owner_id = :ownerId
              and (cast(:status as varchar) is null or o.status = cast(:status as varchar))
        """,
        nativeQuery = true,
    )
    fun countByRestaurantOwner(@Param("ownerId") ownerId: UUID, @Param("status") status: String?): Long

    @Query(
        value = """
            select o.* from orders o
            where o.courier_id = :courierId
              and (cast(:status as varchar) is null or o.status = cast(:status as varchar))
            order by o.created_at desc, o.order_id desc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun pageByCourier(
        @Param("courierId") courierId: UUID,
        @Param("status") status: String?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<OrderEntity>

    @Query(
        value = """
            select count(*) from orders o
            where o.courier_id = :courierId
              and (cast(:status as varchar) is null or o.status = cast(:status as varchar))
        """,
        nativeQuery = true,
    )
    fun countByCourier(@Param("courierId") courierId: UUID, @Param("status") status: String?): Long

    @Query(value = "select * from orders where order_id = :orderId for update", nativeQuery = true)
    fun findByIdForUpdate(@Param("orderId") orderId: UUID): OrderEntity?

    @Query(
        value = """
            select exists(
                select 1 from orders
                where courier_id = :courierId and status in ('READY', 'IN_DELIVERY')
            )
        """,
        nativeQuery = true,
    )
    fun existsActiveByCourier(@Param("courierId") courierId: UUID): Boolean

    @Modifying
    @Query(
        value = """
            with picked_order as (
                select order_id from orders
                where status = 'READY' and courier_id is null
                order by created_at, order_id
                limit 1
                for update skip locked
            ),
            picked_courier as (
                select u.user_id from users u
                where u.courier_mode_enabled = true
                  and not exists (
                      select 1 from orders o
                      where o.courier_id = u.user_id and o.status in ('READY', 'IN_DELIVERY')
                  )
                order by random()
                limit 1
                for update skip locked
            )
            update orders
            set courier_id = (select user_id from picked_courier), updated_at = :now
            where order_id = (select order_id from picked_order)
              and exists (select 1 from picked_courier)
        """,
        nativeQuery = true,
    )
    fun assignFreeCourier(@Param("now") now: Instant): Int
}
