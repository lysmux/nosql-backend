package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl.migrations

import io.mongock.api.annotations.ChangeUnit
import io.mongock.api.annotations.Execution
import io.mongock.api.annotations.RollbackExecution
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.index.Index
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl.OrderDocument

@ChangeUnit(
    id = "002-index-orders-by-owner-and-status",
    order = "002",
    author = "lysmux",
    transactional = false,
)
class IndexOrdersByOwnerAndStatus {
    @Execution
    fun execute(mongo: MongoTemplate) {
        val indexOps = mongo.indexOps(OrderDocument.COLLECTION)

        CreateOrderSchema.ownerIndexes.forEach { indexOps.dropIndex(it.name) }
        ownerStatusIndexes.forEach { indexOps.createIndex(it) }
    }

    @RollbackExecution
    fun rollback(mongo: MongoTemplate) {
        val indexOps = mongo.indexOps(OrderDocument.COLLECTION)

        ownerStatusIndexes.forEach { indexOps.dropIndex(it.name) }
        CreateOrderSchema.ownerIndexes.forEach { indexOps.createIndex(it) }
    }

    companion object {
        private fun ownerStatusNewestFirst(owner: String, name: String) =
            Index().on(owner, Sort.Direction.ASC)
                .on("status", Sort.Direction.ASC)
                .on("createdAt", Sort.Direction.DESC)
                .on("_id", Sort.Direction.DESC)
                .named(name)

        val ownerStatusIndexes = listOf(
            ownerStatusNewestFirst("userId", "orders_user_status_idx"),
            ownerStatusNewestFirst("restaurantId", "orders_restaurant_status_idx"),
            ownerStatusNewestFirst("courierId", "orders_courier_status_idx"),
        )

        private val Index.name: String
            get() = requireNotNull(indexOptions.getString("name"))
    }
}
