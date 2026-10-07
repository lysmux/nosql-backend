import "@typespec/http";
using TypeSpec.Http;
namespace DeliveryService;

@format("uuid")
scalar uuid extends string;

@format("email")
@maxLength(254)
scalar email extends string;

@secret
@minLength(8)
@maxLength(128)
scalar password extends string;

@minValue(0)
@maxValue(999999999.99)
@doc("Сумма в рублях; не более двух знаков после запятой")
@TypeSpec.OpenAPI.extension("multipleOf", 0.01)
scalar money extends decimal;

enum ErrorCode {
  validationFailed: "validation_failed",
  invalidCredentials: "invalid_credentials",
  sessionExpired: "session_expired",
  forbidden: "forbidden",
  notFound: "not_found",
  conflict: "conflict",
  serviceUnavailable: "service_unavailable",
  internalError: "internal_error",
}

model ErrorBody {
  code: ErrorCode;
  message: string;
}
@error @doc("Некорректный запрос: тело или параметры не прошли валидацию") model BadRequest extends ErrorBody { @statusCode statusCode: 400; }
@error @doc("Требуется действующая сессия") model Unauthorized extends ErrorBody { @statusCode statusCode: 401; }
@error @doc("Недостаточно прав для операции") model Forbidden extends ErrorBody { @statusCode statusCode: 403; }
@error @doc("Объект не найден") model NotFound extends ErrorBody { @statusCode statusCode: 404; }
@error @doc("Операция конфликтует с текущим состоянием") model Conflict extends ErrorBody { @statusCode statusCode: 409; }
@error @doc("Хранилище временно недоступно") model ServiceUnavailable extends ErrorBody { @statusCode statusCode: 503; }
@error @doc("Непредвиденная ошибка сервера") model InternalError extends ErrorBody { @statusCode statusCode: 500; }

@error @doc("Неверные email или пароль") model InvalidCredentials extends ErrorBody { @statusCode statusCode: 401; }
@error @doc("Email уже зарегистрирован") model EmailConflict extends ErrorBody { @statusCode statusCode: 409; }
@error @doc("Нельзя выключить режим курьера при активном назначении") model CourierModeConflict extends ErrorBody { @statusCode statusCode: 409; }
@error @doc("Заказ не существует или недоступен текущему аккаунту") model OrderNotFound extends ErrorBody { @statusCode statusCode: 404; }
@error @doc("Недопустимый переход из текущего статуса заказа") model OrderStatusConflict extends ErrorBody { @statusCode statusCode: 409; }
@error @doc("Блюдо недоступно или архивировано") model OrderItemsConflict extends ErrorBody { @statusCode statusCode: 409; }

alias InfrastructureError = ServiceUnavailable | InternalError;
alias AuthError = Unauthorized | Forbidden;
alias CommandError = BadRequest | AuthError | NotFound | Conflict | InfrastructureError;

alias OrderReadError = BadRequest | AuthError | OrderNotFound | InfrastructureError;
alias OrderCommandError = OrderReadError | OrderStatusConflict;
alias OrderCreateError = BadRequest | AuthError | NotFound | OrderItemsConflict | InfrastructureError;

model Pagination {
  @query @minValue(0) page?: int32 = 0;
  @query @minValue(1) @maxValue(100) size?: int32 = 20;
}
model Page<T> {
  items: T[];
  @minValue(0) page: int32;
  @minValue(1) @maxValue(100) pageSize: int32;
  @minValue(0) totalItems: int64;
  @minValue(0) totalPages: int32;
}
