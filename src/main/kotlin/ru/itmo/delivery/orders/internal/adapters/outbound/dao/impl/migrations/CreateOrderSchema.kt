package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl.migrations

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.PartialIndexFilter
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.schema.JsonSchemaObject
import org.springframework.data.mongodb.core.schema.JsonSchemaObject.Type
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.*
import org.springframework.data.mongodb.core.schema.MongoJsonSchema
import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl.OrderDocument
import ru.itmo.delivery.shared.mongo.MongoTypes.nullableUuid
import ru.itmo.delivery.shared.mongo.MongoTypes.oneOf
import ru.itmo.delivery.shared.mongo.MongoTypes.uuid

@ChangeUnit(
    id = "001-create-orders-schema",
    order = "001",
    author = "lysmux",
)
class CreateOrderSchema {
    @Execution
    fun execute(mongo: MongoTemplate) {
        mongo.createCollection(
            OrderDocument.COLLECTION,
            CollectionOptions.empty().schema(schema),
        )
        indexes.forEach { mongo.indexOps(OrderDocument.COLLECTION).createIndex(it) }
    }

    @RollbackExecution
    fun rollback(mongo: MongoTemplate) {
        mongo.dropCollection(OrderDocument.COLLECTION)
    }

    companion object {
        private val activeAssignment = PartialIndexFilter.of(
            Criteria.where("status").`in`(OrderStatus.READY.name, OrderStatus.IN_DELIVERY.name)
                .and("courierId").type(Type.binaryType()),
        )

        val ownerIndexes = listOf(
            Index().on("userId", Sort.Direction.ASC).named("orders_user_idx"),
            Index().on("restaurantId", Sort.Direction.ASC).named("orders_restaurant_idx"),
        )

        val indexes = ownerIndexes + listOf(
            Index().on("createdAt", Sort.Direction.DESC).on("_id", Sort.Direction.DESC).named("orders_list_idx"),
            Index().on("courierId", Sort.Direction.ASC)
                .unique()
                .named("orders_one_active_per_courier")
                .partial(activeAssignment),
        )

        val schema = MongoJsonSchema.builder()
            .required(
                "userId", "restaurantId", "restaurantName", "items", "deliveryAddress", "totalAmount",
                "status", "history", "createdAt", "updatedAt",
            )
            .properties(
                uuid("userId"),
                uuid("restaurantId"),
                nullableUuid("courierId"),
                decimal128("totalAmount"),
                oneOf("status", OrderStatus.entries),
                date("createdAt"),
                date("updatedAt"),
                array("items").minItems(1).items(
                    JsonSchemaObject.`object`()
                        .required("menuItemId", "name", "unitPrice", "quantity", "lineTotal")
                        .properties(
                            uuid("menuItemId"),
                            decimal128("unitPrice"),
                            int32("quantity").gte(1),
                            decimal128("lineTotal"),
                        ),
                ),
                array("history").minItems(1).items(
                    JsonSchemaObject.`object`()
                        .required("status", "actorType", "reason", "createdAt")
                        .properties(
                            oneOf("status", OrderStatus.entries),
                            oneOf("actorType", ActorType.entries),
                            date("createdAt"),
                        ),
                ),
            )
            .build()
    }
}
