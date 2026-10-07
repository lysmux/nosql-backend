package ru.itmo.delivery.auth.internal.adapters.outbound.dao.impl

import org.springframework.stereotype.Repository
import ru.itmo.delivery.auth.api.models.Session
import ru.itmo.delivery.auth.internal.adapters.outbound.dao.SessionsDAO
import ru.itmo.delivery.shared.riak.RiakTemplate
import tools.jackson.core.JacksonException
import java.time.Instant
import java.util.UUID

@Repository
class SessionsRiakDao(
    private val riak: RiakTemplate,
) : SessionsDAO {
    private data class StoredSession(val userId: UUID, val expiresAt: Instant)

    override fun save(session: Session) =
        riak.put(BUCKET, session.token, StoredSession(session.userId, session.expiresAt))

    override fun get(token: String): Session? =
        try {
            riak.get<StoredSession>(BUCKET, token)?.let { Session(token, it.userId, it.expiresAt) }
        } catch (_: JacksonException) {
            delete(token)
            null
        }

    override fun delete(token: String) = riak.delete(BUCKET, token)

    private companion object {
        const val BUCKET = "sessions"
    }
}
