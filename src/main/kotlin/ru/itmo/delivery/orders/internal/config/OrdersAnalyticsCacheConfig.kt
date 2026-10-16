package ru.itmo.delivery.orders.internal.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.cache.analytics")
data class OrdersAnalyticsCacheConfig(
    val ttl: Duration = Duration.ofMinutes(1),
)
