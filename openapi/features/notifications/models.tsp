using TypeSpec.Events;
namespace DeliveryService;

enum NotificationEventType { ORDER_CREATED, ORDER_STATUS_CHANGED }
model Notification {
  notificationId: uuid;
  userId: uuid;
  orderId: uuid;
  eventType: NotificationEventType;
  status: OrderStatus;
  text: string;
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
