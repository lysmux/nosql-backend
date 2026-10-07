using TypeSpec.Http;
using TypeSpec.Http.Streams;
namespace DeliveryService;

@route("/notifications")
@tag("Уведомления")
namespace Notifications {
  @get
  @doc("Только пользователь: своя сохранённая лента, createdAt DESC, notificationId DESC. Нет признака/операции прочтения. После обрыва SSE клиент перечитывает ленту и объединяет по notificationId")
  op feed(...Pagination): NotificationPage | BadRequest | AuthError | InfrastructureError;

  @get @route("/stream")
  @doc("""
    Только пользователь. Проверяется cookie session_token.
    Новые уведомления доставляются событием notification: id=notificationId, data=JSON Notification.
    Поток доставляет только живые события; Last-Event-ID не обеспечивает replay и не используется для восстановления.
    После переподключения читать GET /notifications; дедупликация по notificationId.
    Комментарии heartbeat отправляются раз в 15 секунд и не продлевают сессию.
    При выходе или истечении сессии поток закрывается; повторное подключение отклоняется.
    Ошибки JSON возможны только до открытия потока; при сбое открытый поток закрывается
    """)
  op stream(): {
    ...HttpStream<NotificationEvents, "text/event-stream", NotificationEventStream>;
    @header("Cache-Control") cacheControl: "no-cache";
  } | AuthError | InfrastructureError;
}
