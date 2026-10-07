package ru.itmo.delivery.shared.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class MetricsAdapter(
    private val registry: MeterRegistry,
) {
    fun <T> timed(name: String, vararg tags: Pair<String, String>, action: () -> T): T {
        val sample = Timer.start(registry)

        try {
            return action()
        } finally {
            sample.stop(timer(name, tags.toMap()))
        }
    }

    fun <T> timed(name: String, tagsOf: (T) -> Map<String, String>, action: () -> T): T {
        val sample = Timer.start(registry)
        return action().also { sample.stop(timer(name, tagsOf(it))) }
    }

    private fun timer(name: String, tags: Map<String, String>): Timer =
        registry.timer(name, tags.map { (key, value) -> Tag.of(key, value) })
}
