package ru.itmo.delivery.menu.internal.adapters.outbound.dao.impl

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface MenuItemJpaRepository : JpaRepository<MenuItemEntity, UUID> {
    @Query(
        value = """
            select * from menu_items
            where restaurant_id = :restaurantId
              and archived = false
              and (available = true or :onlyAvailable = false)
            order by name asc, menu_item_id asc
        """,
        nativeQuery = true,
    )
    fun findListed(
        @Param("restaurantId") restaurantId: UUID,
        @Param("onlyAvailable") onlyAvailable: Boolean,
    ): List<MenuItemEntity>

    @Query(
        value = """
            select * from menu_items
            where restaurant_id = :restaurantId and menu_item_id = :menuItemId and archived = false
        """,
        nativeQuery = true,
    )
    fun findActive(
        @Param("restaurantId") restaurantId: UUID,
        @Param("menuItemId") menuItemId: UUID,
    ): MenuItemEntity?

    @Query(value = "select * from menu_items where menu_item_id in (:ids)", nativeQuery = true)
    fun findAllByIds(@Param("ids") ids: Collection<UUID>): List<MenuItemEntity>

    @Modifying
    @Query(
        value = """
            update menu_items set archived = true, updated_at = :now
            where restaurant_id = :restaurantId and menu_item_id = :menuItemId and archived = false
        """,
        nativeQuery = true,
    )
    fun markArchived(
        @Param("restaurantId") restaurantId: UUID,
        @Param("menuItemId") menuItemId: UUID,
        @Param("now") now: Instant,
    ): Int

    @Modifying
    @Query(
        value = """
            update menu_items
            set name = :name, description = cast(:description as varchar), price = :price,
                available = :available, updated_at = :updatedAt
            where restaurant_id = :restaurantId and menu_item_id = :menuItemId and archived = false
        """,
        nativeQuery = true,
    )
    fun updateActive(
        @Param("restaurantId") restaurantId: UUID,
        @Param("menuItemId") menuItemId: UUID,
        @Param("name") name: String,
        @Param("description") description: String?,
        @Param("price") price: java.math.BigDecimal,
        @Param("available") available: Boolean,
        @Param("updatedAt") updatedAt: Instant,
    ): Int
}
