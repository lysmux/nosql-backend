import "@typespec/http";
import "@typespec/openapi";
import "@typespec/http/streams";
import "@typespec/events";
import "./common.tsp";
import "./features/auth/models.tsp";
import "./features/auth/resource.tsp";
import "./features/users/models.tsp";
import "./features/users/resource.tsp";
import "./features/restaurants/models.tsp";
import "./features/restaurants/resource.tsp";
import "./features/menu/models.tsp";
import "./features/menu/resource.tsp";
import "./features/orders/models.tsp";
import "./features/orders/resource.tsp";
import "./features/notifications/models.tsp";
import "./features/notifications/resource.tsp";

using TypeSpec.Http;
using TypeSpec.OpenAPI;

@service(#{ title: "Сервис доставки еды" })
@info(#{ version: "1.0.0" })
@server("http://localhost:8080", "Локальный запуск")
@useAuth(ApiKeyAuth<ApiKeyLocation.cookie, "session_token">)
namespace DeliveryService;
