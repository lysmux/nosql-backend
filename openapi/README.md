# HTTP-контракт v1

TypeSpec — источник контрактов. `generated/openapi.yaml` создаётся генератором и не редактируется вручную.

```powershell
cd openapi
npm ci
npm run build -- --warn-as-error
```

Системные правила и модель: [спецификация T01](../docs/superpowers/specs/2026-10-05-delivery-v1-contracts-design.md).

Спецификация эмитится в OpenAPI 3.0: генератор `kotlin-spring` корректно читает в нём `nullable` у ссылок на `uuid` и `allOf` в `OrderDetails`. SSE описан как `HttpStream<NotificationEvents, "text/event-stream", NotificationEventStream>`: событие `notification` содержит JSON `Notification` в `data`, поле `id` равно `notificationId`. Тело потока — именованный скаляр `NotificationEventStream`, который сборка отображает на `SseEmitter` (`schemaMappings` в `build.gradle.kts`); YAML генерируется без ручных изменений.

Это целевой контракт. Старые Kotlin-контроллеры ещё реализуют API оператора и требуют переноса в последующих задачах. Успешная генерация спецификации не означает готовность backend.

## Примеры

Оформление: `POST /orders` с пользовательской cookie `session_token`:

```json
{
  "restaurantId": "b5e223b8-b99c-4a0a-af2a-94c1d238ce19",
  "items": [{ "menuItemId": "74ed5a64-5df0-4fca-b283-4084270f6cab", "quantity": 2 }],
  "deliveryAddress": "Санкт-Петербург, ул. Примерная, д. 1, кв. 2"
}
```

Списки: `GET /orders?scope=CUSTOMER&page=0&size=20`, `GET /orders?scope=COURIER&status=READY`. Scope RESTAURANT требует ресторанный аккаунт; COURIER требует включённый режим курьера.

SSE-кадр из `GET /notifications/stream`:

```text
id: 50344a19-7f91-46a0-b7c8-399ac8e606ae
event: notification
data: {"notificationId":"50344a19-7f91-46a0-b7c8-399ac8e606ae","userId":"851974fe-a65b-4ee9-9f55-dafcbf182a11","orderId":"6f7a3dd1-8e8f-4ec9-b19d-9a04a2c11ea1","eventType":"ORDER_STATUS_CHANGED","status":"READY","text":"Заказ готов","createdAt":"2026-10-05T12:00:00Z"}

```

После разрыва соединения клиент перечитывает `GET /notifications` и объединяет уведомления по `notificationId`. SSE не поддерживает replay по Last-Event-ID.
