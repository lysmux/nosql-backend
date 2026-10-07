package ru.itmo.delivery.core.config

import com.typesafe.config.ConfigFactory
import org.springframework.boot.EnvironmentPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource

class HoconEnvironmentPostProcessor : EnvironmentPostProcessor {

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication,
    ) {
        val config = ConfigFactory.load()
        val properties = config.entrySet().associate { entry ->
            entry.key to config.getAnyRef(entry.key)
        }
        val propertySource = MapPropertySource("hocon", properties)
        environment.propertySources.addFirst(propertySource)
    }
}
