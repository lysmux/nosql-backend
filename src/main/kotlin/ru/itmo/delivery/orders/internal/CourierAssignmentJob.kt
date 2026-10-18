package ru.itmo.delivery.orders.internal

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import ru.itmo.delivery.orders.internal.config.AssignmentConfig
import ru.itmo.delivery.users.api.Users
import java.util.UUID

@Component
class CourierAssignmentJob(
    private val assigner: CourierAssigner,
    private val users: Users,
    private val config: AssignmentConfig,
) {
    fun assignPendingOrders() {
        val couriers = users.courierIds()
        if (couriers.isEmpty()) return

        repeat(config.maxPerTick) {
            if (assignOne(couriers) == 0) return
        }
    }

    private fun assignOne(couriers: Collection<UUID>): Int =
        try {
            if (assigner.assignOne(couriers)) 1 else 0
        } catch (e: DataIntegrityViolationException) {
            log.warn("Courier assignment lost a race on the active-order unique index, retrying on a fresh snapshot")
            log.debug("Assignment contention details", e)
            LOST_RACE
        }

    private companion object {
        const val LOST_RACE = -1
        private val log = LoggerFactory.getLogger(CourierAssignmentJob::class.java)
    }
}
