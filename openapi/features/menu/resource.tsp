using TypeSpec.Http;
namespace DeliveryService;

@tag("Меню")
namespace Menu {
  @get @route("/restaurants/{restaurantId}/menu-items") @useAuth(NoAuth)
  @doc("Меню ресторана; includeUnavailable показывает недоступные блюда владельцу. Архивированные блюда не возвращаются. Сортировка name ASC, menuItemId ASC")
  op list(@path restaurantId: uuid, @query includeUnavailable?: boolean): MenuItem[] | BadRequest | Unauthorized | Forbidden | NotFound | InfrastructureError;

  @post @route("/restaurants/{restaurantId}/menu-items")
  @doc("Создать блюдо в своём ресторане")
  op create(@path restaurantId: uuid, @body body: CreateMenuItemRequest): {
    @statusCode statusCode: 201;
    @body item: MenuItem;
  } | CommandError;

  @patch @route("/restaurants/{restaurantId}/menu-items/{menuItemId}")
  @doc("Изменить своё неархивированное блюдо")
  op update(@path restaurantId: uuid, @path menuItemId: uuid, @body body: UpdateMenuItemRequest): MenuItem | CommandError;

  @delete @route("/restaurants/{restaurantId}/menu-items/{menuItemId}")
  @doc("Архивирует блюдо своего ресторана, исключая его из нового заказа и списков меню")
  op remove(@path restaurantId: uuid, @path menuItemId: uuid): {
    @statusCode statusCode: 204;
  } | CommandError;
}
