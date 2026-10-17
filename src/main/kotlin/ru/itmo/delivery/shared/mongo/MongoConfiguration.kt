package ru.itmo.delivery.shared.mongo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager

@Configuration
class MongoConfiguration {
    @Bean
    fun transactionManager(factory: MongoDatabaseFactory) = MongoTransactionManager(factory)
}
