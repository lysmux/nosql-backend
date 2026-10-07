package ru.itmo.delivery.orders.internal

import ru.itmo.delivery.orders.api.exceptions.OrderStatusConflictException
import ru.itmo.delivery.orders.api.models.ActorType
import ru.itmo.delivery.orders.api.models.OrderStatus
import ru.itmo.delivery.shared.exceptions.ForbiddenException

enum class OrderAction {
    START_PREPARATION,
    MARK_READY,
    PICKUP,
    DELIVER,
    CANCEL,
}

enum class OrderRole {
    RESTAURANT_OWNER,
    COURIER,
    CUSTOMER,
}

data class OrderTransition(
    val action: OrderAction,
    val role: OrderRole,
    val from: Set<OrderStatus>,
    val to: OrderStatus,
    val actorType: ActorType,
    val reason: String,
)

object OrderTransitions {
    private val terminal = setOf(OrderStatus.DELIVERED, OrderStatus.CANCELLED)

    val table: List<OrderTransition> = listOf(
        OrderTransition(
            OrderAction.START_PREPARATION,
            OrderRole.RESTAURANT_OWNER,
            setOf(OrderStatus.CREATED),
            OrderStatus.COOKING,
            ActorType.RESTAURANT,
            "приготовление начато",
        ),
        OrderTransition(
            OrderAction.MARK_READY,
            OrderRole.RESTAURANT_OWNER,
            setOf(OrderStatus.COOKING),
            OrderStatus.READY,
            ActorType.RESTAURANT,
            "заказ готов к выдаче",
        ),
        OrderTransition(
            OrderAction.PICKUP,
            OrderRole.COURIER,
            setOf(OrderStatus.READY),
            OrderStatus.IN_DELIVERY,
            ActorType.USER,
            "курьер забрал заказ",
        ),
        OrderTransition(
            OrderAction.DELIVER,
            OrderRole.COURIER,
            setOf(OrderStatus.IN_DELIVERY),
            OrderStatus.DELIVERED,
            ActorType.USER,
            "заказ доставлен",
        ),
        OrderTransition(
            OrderAction.CANCEL,
            OrderRole.RESTAURANT_OWNER,
            setOf(OrderStatus.CREATED, OrderStatus.COOKING, OrderStatus.READY),
            OrderStatus.CANCELLED,
            ActorType.RESTAURANT,
            "отменён рестораном",
        ),
        OrderTransition(
            OrderAction.CANCEL,
            OrderRole.CUSTOMER,
            setOf(OrderStatus.CREATED),
            OrderStatus.CANCELLED,
            ActorType.USER,
            "отменён клиентом",
        ),
    )

    fun resolve(action: OrderAction, roles: Set<OrderRole>, status: OrderStatus): OrderTransition {
        val permitted = table.filter { it.action == action && it.role in roles }
        if (permitted.isEmpty()) throw ForbiddenException("Действие недоступно для текущего пользователя")

        permitted.firstOrNull { status in it.from }?.let { return it }

        if (status in terminal || permitted.any { it.role != OrderRole.CUSTOMER }) {
            throw OrderStatusConflictException("Переход недопустим из статуса $status")
        }
        throw ForbiddenException("Клиент может отменить заказ только до начала приготовления")
    }
}
