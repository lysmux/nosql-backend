namespace DeliveryService;

model MenuItem {
  menuItemId: uuid;
  restaurantId: uuid;
  name: string;
  description?: string;
  price: money;
  available: boolean;
  createdAt: utcDateTime;
  updatedAt: utcDateTime;
}
model CreateMenuItemRequest {
  @minLength(1) @maxLength(200) name: string;
  @maxLength(2000) description?: string;
  price: money;
  available?: boolean = true;
}
@doc("Хотя бы одно поле обязательно. description=null удаляет описание")
@TypeSpec.OpenAPI.extension("minProperties", 1)
model UpdateMenuItemRequest {
  @minLength(1) @maxLength(200) name?: string;
  @maxLength(2000) description?: string | null;
  price?: money;
  available?: boolean;
}
