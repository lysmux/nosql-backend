package ru.itmo.delivery.orders.internal.config

import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import ru.itmo.delivery.orders.internal.CourierAssignmentJob

@Configuration
@EnableScheduling
class SchedulingConfig(
    private val assignment: AssignmentConfig,
    private val job: CourierAssignmentJob,
) : SchedulingConfigurer {
    override fun configureTasks(registrar: ScheduledTaskRegistrar) {
        if (assignment.enabled) {
            registrar.addFixedDelayTask(job::assignPendingOrders, assignment.interval)
        }
    }
}
