using TypeSpec.Http;
namespace DeliveryService;

@route("/orders")
@tag("Заказы")
namespace Orders {
  @post
  @doc("Только пользователь. Проверяет доступность блюд, считает итоговую сумму и создаёт заказ в статусе CREATED")
  op create(@body body: CreateOrderRequest): {
    @statusCode statusCode: 201;
    @body order: OrderDetails;
  } | OrderCreateError;

  @get
  @doc("scope обязателен: CUSTOMER — свои покупки; RESTAURANT — заказы моих ресторанов (опционально фильтр restaurantId); COURIER — мои назначения. Фильтр status; поиск по вложенным полям: itemName — часть названия позиции без учёта регистра, menuItemId — есть позиция с этим блюдом, cancelledBy — заказ отменён этим актором (по истории). Фильтры сочетаются через И. sortBy — поле сортировки, sortDirection — направление; по умолчанию createdAt по убыванию, при равенстве orderId в том же направлении")
  op list(
    @query scope: OrderScope,
    @query restaurantId?: uuid,
    @query status?: OrderStatus,
    @query @minLength(1) @maxLength(100) itemName?: string,
    @query menuItemId?: uuid,
    @query cancelledBy?: ActorType,
    @query sortBy?: OrderSortField = OrderSortField.CREATED_AT,
    @query sortDirection?: SortDirection = SortDirection.DESC,
    ...Pagination,
  ): OrderPage | OrderReadError;

  @get @route("/{orderId}")
  @doc("Детали доступны клиенту-владельцу, владельцу ресторана заказа или назначенному курьеру с включённым режимом")
  op read(@path orderId: uuid): OrderDetails | OrderReadError;

  @post @route("/{orderId}/start-preparation")
  @doc("Только владелец ресторана заказа: CREATED → COOKING")
  op startPreparation(@path orderId: uuid): OrderDetails | OrderCommandError;

  @post @route("/{orderId}/mark-ready")
  @doc("Только владелец ресторана заказа: COOKING → READY. Свободный курьер назначается случайно. Если свободных нет, заказ ждёт в READY. Назначение не меняет статус")
  op markReady(@path orderId: uuid): OrderDetails | OrderCommandError;

  @post @route("/{orderId}/pickup")
  @doc("Только назначенный курьер с включённым режимом: READY → IN_DELIVERY. Конкурирует с отменой: применяется только одно из действий")
  op pickup(@path orderId: uuid): OrderDetails | OrderCommandError;

  @post @route("/{orderId}/deliver")
  @doc("Только назначенный курьер с включённым режимом: IN_DELIVERY → DELIVERED. Курьер освобождается")
  op deliver(@path orderId: uuid): OrderDetails | OrderCommandError;

  @post @route("/{orderId}/cancel")
  @doc("Клиент-владелец отменяет только CREATED; владелец ресторана заказа — CREATED, COOKING, READY. Переход в CANCELLED освобождает назначенного курьера. DELIVERED/CANCELLED конечны. Назначенный курьер отменять не может")
  op cancel(@path orderId: uuid): OrderDetails | OrderCommandError;
}
