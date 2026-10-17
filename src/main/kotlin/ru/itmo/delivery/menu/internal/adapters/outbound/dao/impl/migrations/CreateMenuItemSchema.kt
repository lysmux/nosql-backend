package ru.itmo.delivery.menu.internal.adapters.outbound.dao.impl.migrations

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.*
import org.springframework.data.mongodb.core.schema.MongoJsonSchema
import ru.itmo.delivery.menu.internal.adapters.outbound.dao.impl.MenuItemDocument
import ru.itmo.delivery.shared.mongo.MongoTypes.uuid

@ChangeUnit(
    id = "001-create-menu-items-schema",
    order = "001",
    author = "lysmux",
)
class CreateMenuItemSchema {
    @Execution
    fun execute(mongo: MongoTemplate) {
        mongo.createCollection(
            MenuItemDocument.COLLECTION,
            CollectionOptions.empty().schema(schema),
        )
        indexes.forEach {
            mongo.indexOps(MenuItemDocument.COLLECTION)
                .createIndex(it)
        }
    }

    @RollbackExecution
    fun rollback(mongo: MongoTemplate) {
        mongo.dropCollection(MenuItemDocument.COLLECTION)
    }

    companion object {
        val indexes = listOf(
            Index().on("restaurantId", Sort.Direction.ASC)
                .on("name", Sort.Direction.ASC)
                .on("_id", Sort.Direction.ASC)
                .named("menu_items_list_idx"),
        )

        val schema = MongoJsonSchema.builder()
            .required("restaurantId", "name", "price", "available", "archived", "createdAt", "updatedAt")
            .properties(
                uuid("restaurantId"),
                string("name").minLength(1).maxLength(200),
                decimal128("price").gte(0),
                bool("available"),
                bool("archived"),
                date("createdAt"),
                date("updatedAt"),
            )
            .build()
    }
}
