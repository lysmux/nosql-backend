package ru.itmo.delivery.auth.internal

import org.springframework.stereotype.Service
import ru.itmo.delivery.auth.api.Sessions
import ru.itmo.delivery.auth.api.exceptions.SessionExpiredException
import ru.itmo.delivery.auth.api.models.Session
import ru.itmo.delivery.auth.internal.adapters.outbound.dao.SessionsDAO
import ru.itmo.delivery.auth.internal.config.SessionConfig
import ru.itmo.delivery.shared.exceptions.AppException
import ru.itmo.delivery.shared.exceptions.ServiceUnavailableException
import java.time.Instant
import java.util.UUID

@Service
class SessionsService(
    private val dao: SessionsDAO,
    private val config: SessionConfig,
) : Sessions {
    override fun create(userId: UUID): Session =
        storageCall { Session(UUID.randomUUID().toString(), userId, nextExpiry()).also(dao::save) }

    override fun resolve(token: String): UUID = storageCall {
        val session = dao.get(token) ?: throw SessionExpiredException()
        if (session.expiresAt.isBefore(Instant.now())) {
            dao.delete(token)
            throw SessionExpiredException()
        }

        dao.save(session.copy(expiresAt = nextExpiry()))
        session.userId
    }

    override fun isActive(token: String): Boolean = storageCall {
        dao.get(token)?.expiresAt?.isAfter(Instant.now()) ?: false
    }

    override fun logout(token: String) = storageCall { dao.delete(token) }

    private fun nextExpiry(): Instant = Instant.now().plus(config.ttl)

    private fun <T> storageCall(block: () -> T): T =
        try {
            block()
        } catch (exception: AppException) {
            throw exception
        } catch (exception: Exception) {
            throw ServiceUnavailableException("Хранилище сессий недоступно").apply { initCause(exception) }
        }
}
