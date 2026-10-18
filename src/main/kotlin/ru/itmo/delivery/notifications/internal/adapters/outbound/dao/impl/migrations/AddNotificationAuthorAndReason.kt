package ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl.migrations

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.bson.Document
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.`object`
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.string
import org.springframework.data.mongodb.core.schema.MongoJsonSchema
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl.NotificationDocument
import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl.OrderDocument
import ru.itmo.delivery.shared.mongo.MongoTypes.oneOf
import ru.itmo.delivery.shared.mongo.MongoTypes.schemaVersion
import ru.itmo.delivery.shared.mongo.MongoTypes.uuid
import java.time.Duration
import java.util.Date
import java.util.UUID

@ChangeUnit(
    id = "003-add-notification-author-and-reason",
    order = "003",
    author = "lysmux",
    transactional = false,
)
class AddNotificationAuthorAndReason {
    @Execution
    fun execute(mongo: MongoTemplate) {
        mongo.getCollection(NotificationDocument.COLLECTION)
            .find(outdated.criteriaObject)
            .forEach { notification -> migrate(mongo, notification) }

        applyValidator(mongo, schema)
    }

    @RollbackExecution
    fun rollback(mongo: MongoTemplate) {
        applyValidator(mongo, CreateNotificationSchema.schema)

        mongo.updateMulti(
            Query(),
            Update().unset("author").unset("reason").set("schemaVersion", CreateNotificationSchema.SCHEMA_VERSION),
            NotificationDocument.COLLECTION,
        )
    }

    private fun migrate(mongo: MongoTemplate, notification: Document) {
        val source = matchingHistoryEntry(mongo, notification)
        val update = Update()
            .set("author", author(source))
            .set("reason", source?.getString("reason") ?: LEGACY_REASON)
            .set("schemaVersion", SCHEMA_VERSION)

        mongo.updateFirst(
            Query(Criteria.where("_id").`is`(notification.get("_id")).andOperator(outdated)),
            update,
            NotificationDocument.COLLECTION,
        )
    }

    private fun matchingHistoryEntry(mongo: MongoTemplate, notification: Document): Document? {
        val order = mongo.findOne(
            Query(Criteria.where("_id").`is`(notification.get("orderId"))),
            Document::class.java,
            OrderDocument.COLLECTION,
        ) ?: return null
        val notifiedAt = notification.get("createdAt", Date::class.java).toInstant()

        return order.getList("history", Document::class.java).orEmpty()
            .filter { it.getString("status") == notification.getString("status") }
            .minByOrNull { Duration.between(it.get("createdAt", Date::class.java).toInstant(), notifiedAt).abs() }
    }

    private fun author(source: Document?): Document {
        if (source == null) return Document("type", ActorType.SYSTEM.name)

        val author = Document("type", source.getString("actorType"))
        source.get("actorId", UUID::class.java)?.let { author.append("id", it) }

        return author
    }

    private fun applyValidator(mongo: MongoTemplate, validator: MongoJsonSchema) {
        mongo.db.runCommand(
            Document("collMod", NotificationDocument.COLLECTION).append("validator", validator.toDocument()),
        )
    }

    companion object {
        const val SCHEMA_VERSION = 2

        private const val LEGACY_REASON = "legacy"

        private val outdated = Criteria.where("schemaVersion").lt(SCHEMA_VERSION)

        private val addedRequired = listOf("author", "reason")

        private val addedProperties = listOf(
            string("reason").minLength(1),
            `object`("author")
                .required("type")
                .properties(
                    oneOf("type", ActorType.entries),
                    uuid("id"),
                ),
            schemaVersion(SCHEMA_VERSION),
        )

        val schema: MongoJsonSchema = MongoJsonSchema.builder()
            .required(*(CreateNotificationSchema.required + addedRequired).toTypedArray())
            .properties(*(CreateNotificationSchema.properties + addedProperties).toTypedArray())
            .build()
    }
}
