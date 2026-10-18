package ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl.migrations

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.*
import org.springframework.data.mongodb.core.schema.MongoJsonSchema
import ru.itmo.delivery.notifications.api.models.NotificationEventType
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl.NotificationDocument
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.shared.mongo.MongoTypes.oneOf
import ru.itmo.delivery.shared.mongo.MongoTypes.schemaVersion
import ru.itmo.delivery.shared.mongo.MongoTypes.uuid

@ChangeUnit(
    id = "001-create-notifications-schema",
    order = "001",
    author = "lysmux",
)
class CreateNotificationSchema {
    @Execution
    fun execute(mongo: MongoTemplate) {
        mongo.createCollection(
            NotificationDocument.COLLECTION,
            CollectionOptions.empty().schema(schema),
        )
        indexes.forEach {
            mongo.indexOps(NotificationDocument.COLLECTION)
                .createIndex(it)
        }
    }

    @RollbackExecution
    fun rollback(mongo: MongoTemplate) {
        mongo.dropCollection(NotificationDocument.COLLECTION)
    }

    companion object {
        val indexes = listOf(
            Index().on("userId", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
                .on("_id", Sort.Direction.DESC)
                .named("notifications_user_idx"),
        )

        const val SCHEMA_VERSION = 1

        val required = listOf("userId", "orderId", "eventType", "status", "text", "createdAt", "schemaVersion")

        val properties = listOf(
            uuid("userId"),
            uuid("orderId"),
            oneOf("eventType", NotificationEventType.entries),
            oneOf("status", OrderStatus.entries),
            string("text").minLength(1),
            date("createdAt"),
        )

        val schema = MongoJsonSchema.builder()
            .required(*required.toTypedArray())
            .properties(*(properties + schemaVersion(SCHEMA_VERSION)).toTypedArray())
            .build()
    }
}
