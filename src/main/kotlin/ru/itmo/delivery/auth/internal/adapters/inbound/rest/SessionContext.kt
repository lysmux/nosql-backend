package ru.itmo.delivery.auth.internal.adapters.inbound.rest

import org.springframework.stereotype.Component
import org.springframework.web.context.annotation.RequestScope
import ru.itmo.delivery.auth.api.CurrentUserHolder
import java.util.UUID

@Component
@RequestScope
class SessionContext(override var userId: UUID? = null) : CurrentUserHolder
