using TypeSpec.Http;
namespace DeliveryService;

@route("/restaurants")
@tag("Рестораны")
namespace Restaurants {
  @get @useAuth(NoAuth)
  @doc("Каталог ресторанов; mine — только свои")
  op list(@query mine?: boolean, ...Pagination): RestaurantPage | BadRequest | Unauthorized | InfrastructureError;

  @post
  @doc("Создать свой ресторан")
  op create(@body body: CreateRestaurantRequest): {
    @statusCode statusCode: 201;
    @body restaurant: Restaurant;
  } | BadRequest | Unauthorized | InfrastructureError;

  @get @route("/{restaurantId}") @useAuth(NoAuth)
  @doc("Публичный профиль ресторана")
  op read(@path restaurantId: uuid): Restaurant | BadRequest | NotFound | InfrastructureError;

  @patch @route("/{restaurantId}")
  @doc("Редактирование своего ресторана")
  op update(@path restaurantId: uuid, @body body: UpdateRestaurantRequest): Restaurant | BadRequest | AuthError | NotFound | InfrastructureError;

  @post @route("/{restaurantId}/visits") @useAuth(NoAuth)
  @doc("Регистрирует просмотр страницы ресторана: увеличивает счётчик на 1 и возвращает текущее значение")
  op registerVisit(@path restaurantId: uuid): RestaurantVisitCounter | BadRequest | NotFound | InfrastructureError;

  @get @route("/{restaurantId}/visits") @useAuth(NoAuth)
  @doc("Текущее значение счётчика просмотров страницы ресторана")
  op readVisits(@path restaurantId: uuid): RestaurantVisitCounter | BadRequest | NotFound | InfrastructureError;
}
