using TypeSpec.Http;
namespace DeliveryService;

@route("/users/me")
@tag("Личный кабинет")
namespace Users {
  @get
  @doc("Профиль текущего пользователя. Пароль не возвращается")
  op me(): User | AuthError | InfrastructureError;

  @put @route("/courier-mode")
  @doc("Включает или выключает режим курьера у текущего пользователя. Повторная установка того же значения допустима")
  op setCourierMode(@body body: CourierModeRequest): User | BadRequest | AuthError | CourierModeConflict | InfrastructureError;
}
