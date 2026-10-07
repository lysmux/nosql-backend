namespace DeliveryService;

enum OrderStatus { CREATED, COOKING, READY, IN_DELIVERY, DELIVERED, CANCELLED }
enum OrderScope { CUSTOMER, RESTAURANT, COURIER }
enum ActorType { USER, RESTAURANT, SYSTEM }

model StatusHistoryEntry {
  fromStatus: OrderStatus | null;
  toStatus: OrderStatus;
  actorType: ActorType;
  @doc("null только для SYSTEM")
  actorId: uuid | null;
  reason: string;
  createdAt: utcDateTime;
}
model OrderItem {
  menuItemId: uuid;
  @doc("Название блюда на момент оформления")
  name: string;
  @doc("Цена блюда на момент оформления")
  unitPrice: money;
  @minValue(1) @maxValue(99) quantity: int32;
  lineTotal: money;
}
model Order {
  orderId: uuid;
  userId: uuid;
  restaurantId: uuid;
  @doc("Снимок названия ресторана на момент оформления")
  restaurantName: string;
  @minItems(1) @maxItems(100) items: OrderItem[];
  deliveryAddress: string;
  totalAmount: money;
  status: OrderStatus;
  @doc("null до назначения и после отмены; сохраняется после доставки")
  courierId: uuid | null;
  createdAt: utcDateTime;
  updatedAt: utcDateTime;
}
model OrderDetails extends Order {
  @doc("История от первого перехода к последнему")
  history: StatusHistoryEntry[];
}
model OrderPage is Page<Order>;
model OrderItemRequest {
  menuItemId: uuid;
  @minValue(1) @maxValue(99) quantity: int32;
}
@doc("Корзина клиентская. Без userId, courierId, цен и итоговой суммы. Позиции не повторяются и принадлежат одному ресторану; блюда должны быть доступны")
model CreateOrderRequest {
  restaurantId: uuid;
  @minItems(1) @maxItems(100) items: OrderItemRequest[];
  @minLength(1) @maxLength(500) deliveryAddress: string;
}
