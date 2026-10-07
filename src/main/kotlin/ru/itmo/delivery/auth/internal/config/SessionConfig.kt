package ru.itmo.delivery.auth.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.session")
data class SessionConfig(
    val ttl: Duration = DEFAULT_TTL,
) {
    companion object {
        private val DEFAULT_TTL: Duration = Duration.ofMinutes(30)
    }
}
