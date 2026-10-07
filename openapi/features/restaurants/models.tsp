namespace DeliveryService;

@doc("Публичные сведения; email отсутствует")
model Restaurant {
  restaurantId: uuid;
  name: string;
  createdAt: utcDateTime;
}
model CreateRestaurantRequest {
  @minLength(1) @maxLength(200) name: string;
}
model UpdateRestaurantRequest {
  @minLength(1) @maxLength(200) name: string;
}
model RestaurantPage is Page<Restaurant>;
@doc("Счётчик просмотров страницы ресторана для аналитики; считает просмотры, а не уникальных посетителей")
model RestaurantVisitCounter {
  restaurantId: uuid;
  @minValue(0) value: int64;
}
