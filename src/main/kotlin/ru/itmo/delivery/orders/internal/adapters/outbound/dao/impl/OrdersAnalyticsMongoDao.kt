package ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl

import org.bson.Document
import org.bson.types.Decimal128
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository
import ru.itmo.delivery.orders.api.models.RestaurantOrderStats
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.OrdersAnalyticsDAO
import ru.itmo.delivery.restaurants.internal.adapters.outbound.dao.impl.RestaurantDocument
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@Repository
class OrdersAnalyticsMongoDao(
    private val template: MongoTemplate,
) : OrdersAnalyticsDAO {
    override fun statsByRestaurant(restaurantIds: Collection<UUID>): List<RestaurantOrderStats> {
        if (restaurantIds.isEmpty()) return emptyList()

        val pipeline = listOf(
            Document("\$match", Document("restaurantId", Document("\$in", restaurantIds.toList()))),
            Document(
                "\$group",
                Document("_id", "\$restaurantId")
                    .append("total", Document("\$sum", 1))
                    .append("delivered", sumWhenStatus("DELIVERED", 1))
                    .append("cancelled", sumWhenStatus("CANCELLED", 1))
                    .append("revenue", sumWhenStatus("DELIVERED", "\$totalAmount")),
            ),
            Document(
                "\$lookup",
                Document("from", RestaurantDocument.COLLECTION)
                    .append("localField", "_id")
                    .append("foreignField", "_id")
                    .append("as", "restaurant"),
            ),
            Document("\$unwind", "\$restaurant"),
            Document(
                "\$project",
                Document("_id", 0)
                    .append("restaurantId", "\$_id")
                    .append("name", "\$restaurant.name")
                    .append("total", 1)
                    .append("delivered", 1)
                    .append("cancelled", 1)
                    .append("revenue", 1),
            ),
            Document("\$sort", Document("total", -1).append("name", 1)),
        )

        return template.getCollection(OrderDocument.COLLECTION)
            .aggregate(pipeline)
            .map { it.toStats() }
            .toList()
    }

    private fun sumWhenStatus(status: String, value: Any) = Document(
        "\$sum",
        Document("\$cond", listOf(Document("\$eq", listOf("\$status", status)), value, 0)),
    )

    private fun Document.toStats() = RestaurantOrderStats(
        restaurantId = get("restaurantId", UUID::class.java),
        name = getString("name"),
        total = (get("total") as Number).toLong(),
        delivered = (get("delivered") as Number).toLong(),
        cancelled = (get("cancelled") as Number).toLong(),
        revenue = money(get("revenue")),
    )

    private fun money(value: Any?): BigDecimal =
        ((value as? Decimal128)?.bigDecimalValue() ?: BigDecimal(value.toString()))
            .setScale(2, RoundingMode.HALF_UP)
}
