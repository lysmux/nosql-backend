package ru.itmo.delivery.orders.internal

import org.springframework.stereotype.Component
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.impl.OrderJpaRepository
import ru.itmo.delivery.users.api.CourierAvailability
import java.util.UUID

@Component
class OrdersCourierAvailability(
    private val orders: OrderJpaRepository,
) : CourierAvailability {
    override fun existsActiveAssignment(courierId: UUID): Boolean = orders.existsActiveByCourier(courierId)
}
