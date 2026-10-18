using TypeSpec.Http;
namespace DeliveryService;

@route("/analytics")
@tag("Аналитика")
namespace Analytics {
  @get @route("/orders-by-restaurant")
  @doc("Заказы по моим ресторанам: всего, доставлено, отменено и выручка. Сортировка total DESC, name ASC")
  op ordersByRestaurant(): OrdersByRestaurant | AuthError | InfrastructureError;
}
