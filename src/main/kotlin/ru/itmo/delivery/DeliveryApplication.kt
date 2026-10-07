package ru.itmo.delivery

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class DeliveryApplication

fun main(args: Array<String>) {
    runApplication<DeliveryApplication>(*args)
}
