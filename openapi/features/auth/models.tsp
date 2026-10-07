namespace DeliveryService;

model RegisterRequest {
  @minLength(1) @maxLength(120) name: string;
  email: email;
  password: password;
}
model LoginRequest {
  email: email;
  @secret @minLength(1) @maxLength(128) password: string;
}
@doc("Токен передаётся только в HttpOnly cookie, не в теле ответа. Сессия истекает после 30 минут бездействия")
model Session {
  userId: uuid;
  expiresAt: utcDateTime;
}
