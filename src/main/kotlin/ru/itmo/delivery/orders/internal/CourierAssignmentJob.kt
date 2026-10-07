package ru.itmo.delivery.orders.internal

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.OrdersDAO
import ru.itmo.delivery.orders.internal.config.AssignmentConfig
import java.time.Instant

@Component
class CourierAssignmentJob(
    private val dao: OrdersDAO,
    private val config: AssignmentConfig,
) {
    fun assignPendingOrders() {
        repeat(config.maxPerTick) {
            if (assignOne() == 0) return
        }
    }

    private fun assignOne(): Int =
        try {
            dao.assignFreeCourier(Instant.now())
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
