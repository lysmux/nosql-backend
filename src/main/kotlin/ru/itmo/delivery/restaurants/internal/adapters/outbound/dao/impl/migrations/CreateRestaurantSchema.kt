package ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.impl.migrations

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.*
import org.springframework.data.mongodb.core.schema.MongoJsonSchema
import ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.impl.RestaurantDocument
import ru.itmo.delivery.shared.mongo.MongoTypes.uuid

@ChangeUnit(
    id = "001-create-restaurants-schema",
    order = "001",
    author = "lysmux",
)
class CreateRestaurantSchema {
    @Execution
    fun execute(mongo: MongoTemplate) {
        mongo.createCollection(
            RestaurantDocument.COLLECTION,
            CollectionOptions.empty().schema(schema),
        )
        indexes.forEach { mongo.indexOps(RestaurantDocument.COLLECTION).createIndex(it) }
    }

    @RollbackExecution
    fun rollback(mongo: MongoTemplate) {
        mongo.dropCollection(RestaurantDocument.COLLECTION)
    }

    companion object {
        val indexes = listOf(
            Index().on("ownerId", Sort.Direction.ASC).named("restaurants_owner_idx"),
            Index().on("name", Sort.Direction.ASC).on("_id", Sort.Direction.ASC).named("restaurants_catalog_idx"),
        )

        val schema = MongoJsonSchema.builder()
            .required("ownerId", "name", "createdAt")
            .properties(
                uuid("ownerId"),
                string("name").minLength(1).maxLength(200),
                date("createdAt"),
            )
            .build()
    }
}
