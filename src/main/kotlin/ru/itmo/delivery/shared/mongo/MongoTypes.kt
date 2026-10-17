package ru.itmo.delivery.shared.mongo

import org.springframework.data.mongodb.core.schema.JsonSchemaObject
import org.springframework.data.mongodb.core.schema.JsonSchemaObject.Type
import org.springframework.data.mongodb.core.schema.JsonSchemaProperty

object MongoTypes {
    fun uuid(name: String) = JsonSchemaProperty
        .named(name)
        .ofType(Type.binaryType())

    fun nullableUuid(name: String) = JsonSchemaProperty.untyped(name)
        .anyOf(
            listOf(
                JsonSchemaObject.of(Type.binaryType()),
                JsonSchemaObject.of(
                    Type.nullType(),
                ),
            ),
        )

    fun oneOf(name: String, values: Collection<Enum<*>>) = JsonSchemaProperty.named(name)
        .with(
            JsonSchemaObject.of(Type.stringType())
                .possibleValues(values.map { it.name }),
        )

    fun schemaVersion(version: Int) = JsonSchemaProperty.named("schemaVersion")
        .with(
            JsonSchemaObject.of(Type.intType())
                .possibleValues(listOf(version)),
        )
}
