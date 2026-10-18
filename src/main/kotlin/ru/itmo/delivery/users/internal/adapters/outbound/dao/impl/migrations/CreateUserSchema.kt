package ru.itmo.delivery.users.internal.adapters.outbound.dao.impl.migrations

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty.*
import org.springframework.data.mongodb.core.schema.MongoJsonSchema
import ru.itmo.delivery.users.internal.adapters.outbound.dao.impl.UserDocument

@ChangeUnit(
    id = "001-create-user-schema",
    order = "001",
    author = "lysmux",
)
class CreateUserSchema {
    @Execution
    fun execute(mongo: MongoTemplate) {
        mongo.createCollection(
            UserDocument.COLLECTION,
            CollectionOptions.empty().schema(schema),
        )
        indexes.forEach {
            mongo.indexOps(UserDocument.COLLECTION)
                .createIndex(it)
        }
    }

    @RollbackExecution
    fun rollback(mongo: MongoTemplate) {
        mongo.dropCollection(UserDocument.COLLECTION)
    }

    companion object {
        val indexes = listOf(
            Index().on("email", Sort.Direction.ASC)
                .unique()
                .named("users_email_key"),
        )

        val schema = MongoJsonSchema.builder()
            .required("name", "email", "passwordHash", "courierModeEnabled", "createdAt")
            .properties(
                string("name").minLength(1),
                string("email").minLength(1),
                string("passwordHash").minLength(1),
                bool("courierModeEnabled"),
                date("createdAt"),
            )
            .build()
    }
}
