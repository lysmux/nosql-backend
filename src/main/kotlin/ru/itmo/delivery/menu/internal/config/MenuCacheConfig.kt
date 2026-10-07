package ru.itmo.delivery.menu.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.cache.menu")
data class MenuCacheConfig(
    val ttl: Duration = Duration.ofMinutes(1),
)
