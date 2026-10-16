package ru.itmo.delivery.notifications.internal.adapters.outbound.dao.impl

import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.NotificationRecord
import ru.itmo.delivery.notifications.internal.adapters.outbound.dao.NotificationsDAO
import ru.itmo.delivery.shared.models.Paged
import java.util.UUID

@Repository
class NotificationsMongoDao(
    private val template: MongoTemplate,
) : NotificationsDAO {
    override fun save(notification: NotificationRecord) {
        template.insert(notification.toDocument())
    }

    override fun findById(notificationId: UUID): NotificationRecord? =
        template.findById(notificationId, NotificationDocument::class.java)?.toRecord()

    override fun findPage(userId: UUID, page: Int, size: Int): Paged<NotificationRecord> {
        val query = Query(Criteria.where("userId").`is`(userId))
        val total = template.count(query, NotificationDocument::class.java)

        query.with(Sort.by(Sort.Direction.DESC, "createdAt", "_id")).skip(page.toLong() * size).limit(size)
        val items = template.find(query, NotificationDocument::class.java).map { it.toRecord() }
        return Paged(items, page, size, total)
    }

    private fun NotificationRecord.toDocument() = NotificationDocument(
        id = notificationId,
        userId = userId,
        orderId = orderId,
        eventType = eventType,
        status = status,
        text = text,
        createdAt = createdAt,
        author = AuthorDocument(author.type, author.id),
        reason = reason,
        schemaVersion = NOTIFICATION_SCHEMA_VERSION,
    )
}
