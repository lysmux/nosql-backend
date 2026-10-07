package ru.itmo.delivery.auth.internal.adapters.outbound.dao

import ru.itmo.delivery.auth.api.models.Session

interface SessionsDAO {
    fun save(session: Session)

    fun get(token: String): Session?

    fun delete(token: String)
}
