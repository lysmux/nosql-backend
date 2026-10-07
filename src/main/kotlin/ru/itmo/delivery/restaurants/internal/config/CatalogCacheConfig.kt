package ru.itmo.delivery.restaurants.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.cache.catalog")
data class CatalogCacheConfig(
    val ttl: Duration = Duration.ofMinutes(1),
)
