package ru.itmo.delivery.shared.riak

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.riak")
data class RiakConfig(
    val nodes: List<String>,
    val timeout: Duration = DEFAULT_TIMEOUT,
) {
    companion object {
        private val DEFAULT_TIMEOUT: Duration = Duration.ofSeconds(2)
    }
}
