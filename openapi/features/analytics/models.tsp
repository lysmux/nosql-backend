namespace DeliveryService;

model RestaurantOrderStats {
  restaurantId: uuid;
  name: string;
  total: int64;
  delivered: int64;
  cancelled: int64;
  @doc("Сумма totalAmount доставленных заказов")
  revenue: money;
}
model OrdersByRestaurant {
  items: RestaurantOrderStats[];
}
