package ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.impl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface RestaurantJpaRepository : JpaRepository<RestaurantEntity, UUID> {
    @Query(
        value = """
            select * from restaurants
            order by name asc, restaurant_id asc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun findAllPage(@Param("limit") limit: Int, @Param("offset") offset: Long): List<RestaurantEntity>

    @Query(value = "select * from restaurants order by name asc, restaurant_id asc", nativeQuery = true)
    fun findAllOrdered(): List<RestaurantEntity>

    @Query(value = "select count(*) from restaurants", nativeQuery = true)
    fun countAll(): Long

    @Query(
        value = """
            select * from restaurants
            where owner_id = :ownerId
            order by name asc, restaurant_id asc
            limit :limit offset :offset
        """,
        nativeQuery = true,
    )
    fun findPageByOwner(
        @Param("ownerId") ownerId: UUID,
        @Param("limit") limit: Int,
        @Param("offset") offset: Long,
    ): List<RestaurantEntity>

    @Query(value = "select count(*) from restaurants where owner_id = :ownerId", nativeQuery = true)
    fun countByOwner(@Param("ownerId") ownerId: UUID): Long
}
