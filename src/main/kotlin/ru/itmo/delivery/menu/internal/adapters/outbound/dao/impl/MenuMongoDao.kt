package ru.itmo.delivery.menu.internal.adapters.outbound.dao.impl

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import ru.itmo.delivery.menu.api.models.MenuItem
import ru.itmo.delivery.menu.api.models.OrderMenuItem
import ru.itmo.delivery.menu.internal.adapters.outbound.dao.MenuDAO
import java.time.Instant
import java.util.UUID

@Repository
class MenuMongoDao(
    private val template: MongoTemplate,
) : MenuDAO {
    override fun save(item: MenuItem) {
        template.save(
            MenuItemDocument(
                id = item.menuItemId,
                restaurantId = item.restaurantId,
                name = item.name,
                description = item.description,
                price = item.price,
                available = item.available,
                archived = false,
                createdAt = item.createdAt,
                updatedAt = item.updatedAt,
            ),
        )
    }

    override fun updateActive(item: MenuItem): Boolean =
        template.updateFirst(
            active(item.restaurantId, item.menuItemId),
            Update()
                .set("name", item.name)
                .set("description", item.description)
                .set("price", item.price)
                .set("available", item.available)
                .set("updatedAt", item.updatedAt),
            MenuItemDocument::class.java,
        ).matchedCount > 0

    override fun findActive(restaurantId: UUID, menuItemId: UUID): MenuItem? =
        template.findOne(active(restaurantId, menuItemId), MenuItemDocument::class.java)?.toMenuItem()

    override fun findAll(restaurantId: UUID, onlyAvailable: Boolean): List<MenuItem> {
        val listed = Criteria.where("restaurantId").`is`(restaurantId).and("archived").`is`(false)
        if (onlyAvailable) listed.and("available").`is`(true)

        return template.find(
            Query(listed).with(Sort.by(Sort.Direction.ASC, "name", "_id")),
            MenuItemDocument::class.java,
        ).map { it.toMenuItem() }
    }

    override fun findForOrder(menuItemIds: Collection<UUID>): List<OrderMenuItem> =
        template.find(Query(Criteria.where("_id").`in`(menuItemIds)), MenuItemDocument::class.java).map {
            OrderMenuItem(
                menuItemId = it.id,
                restaurantId = it.restaurantId,
                name = it.name,
                price = it.price,
                available = it.available,
                archived = it.archived,
            )
        }

    override fun archive(restaurantId: UUID, menuItemId: UUID, now: Instant): Boolean =
        template.updateFirst(
            active(restaurantId, menuItemId),
            Update().set("archived", true).set("updatedAt", now),
            MenuItemDocument::class.java,
        ).matchedCount > 0

    private fun active(restaurantId: UUID, menuItemId: UUID) = Query(
        Criteria.where("restaurantId").`is`(restaurantId)
            .and("_id").`is`(menuItemId)
            .and("archived").`is`(false),
    )

    private fun MenuItemDocument.toMenuItem() = MenuItem(
        menuItemId = id,
        restaurantId = restaurantId,
        name = name,
        description = description,
        price = price,
        available = available,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
