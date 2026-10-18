package ru.itmo.delivery.orders.internal

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import ru.itmo.delivery.orders.internal.adapters.outbound.dao.OrdersDAO
import ru.itmo.delivery.users.api.Users
import java.util.UUID

@Component
class CourierAssigner(
    private val dao: OrdersDAO,
    private val users: Users,
) {
    @Transactional
    fun assignOne(couriers: Collection<UUID>): Boolean {
        val assignment = dao.assignFreeCourier(couriers) ?: return false
        users.assignActiveOrder(assignment.courierId, assignment.orderId)
        return true
    }
}
