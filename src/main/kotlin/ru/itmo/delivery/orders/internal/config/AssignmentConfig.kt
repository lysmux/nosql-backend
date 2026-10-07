package ru.itmo.delivery.orders.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.assignment")
data class AssignmentConfig(
    val enabled: Boolean = true,
    val interval: Duration = DEFAULT_INTERVAL,
    val maxPerTick: Int = DEFAULT_MAX_PER_TICK,
) {
    companion object {
        private val DEFAULT_INTERVAL: Duration = Duration.ofSeconds(5)
        private const val DEFAULT_MAX_PER_TICK = 100
    }
}
