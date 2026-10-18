using TypeSpec.Events;
namespace DeliveryService;

enum NotificationEventType { ORDER_CREATED, ORDER_STATUS_CHANGED }
@doc("Кто перевёл заказ в статус, породивший уведомление")
model NotificationAuthor {
  type: ActorType;
  @doc("null только для SYSTEM")
  id: uuid | null;
}
model Notification {
  notificationId: uuid;
  userId: uuid;
  orderId: uuid;
  eventType: NotificationEventType;
  status: OrderStatus;
  text: string;
  author: NotificationAuthor;
  @doc("Причина перехода; legacy у уведомлений, созданных до появления поля")
  reason: string;
  createdAt: utcDateTime;
}
model NotificationPage is Page<Notification>;

@doc("Кадр SSE: id совпадает с data.notificationId, имя события notification")
model NotificationFrame {
  id: uuid;
  @data data: Notification;
}
@events
union NotificationEvents {
  @TypeSpec.OpenAPI.extension("allOf", #[
    #{
      type: "object",
      properties: #{ id: #{ type: "string", format: "uuid" } },
      required: #["id", "data"],
    }
  ])
  notification: NotificationFrame,
}

@doc("Поток SSE с кадрами NotificationFrame")
scalar NotificationEventStream extends string;
