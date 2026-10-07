using TypeSpec.Http;
namespace DeliveryService;

@route("/auth")
@tag("Аутентификация")
namespace Auth {
  @post @route("/register") @useAuth(NoAuth)
  @doc("Регистрация пользователя без автоматического входа. Email нормализуется (trim/lowercase) и уникален. courierModeEnabled=false")
  op register(@body body: RegisterRequest): {
    @statusCode statusCode: 201;
  } | BadRequest | EmailConflict | InfrastructureError;

  @post @route("/login") @useAuth(NoAuth)
  @doc("Вход пользователя по email и паролю: создаёт сессию и ставит cookie session_token. Успешный защищённый запрос продлевает TTL сессии до 30 минут")
  op login(@body body: LoginRequest): {
    @header("Set-Cookie") setCookie: string;
    @body session: Session;
  } | BadRequest | InvalidCredentials | InfrastructureError;

  @post @route("/logout")
  @doc("Удаляет текущую сессию и сбрасывает cookie")
  op logout(): {
    @statusCode statusCode: 204;
    @header("Set-Cookie") setCookie: string;
  } | AuthError | InfrastructureError;
}
