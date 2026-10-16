// Fills the orders collection with synthetic data. Run against a scratch database, not the application one:
//   docker compose cp docker/mongo/seed-orders.js mongo:/tmp/ && docker compose exec -T mongo mongosh --quiet delivery_explain --file /tmp/seed-orders.js
const ORDERS = 100000;
const CUSTOMERS = 50;
const RESTAURANTS = 20;
const COURIERS = 30;
const BATCH = 5000;
const statuses = ["CREATED", "COOKING", "READY", "DELIVERED", "CANCELLED"];
const dishes = ["Margherita", "Pepperoni", "Sushi set", "Burger", "Caesar", "Cola", "Ramen", "Tea"];

const customers = Array.from({length: CUSTOMERS}, () => UUID());
const restaurants = Array.from({length: RESTAURANTS}, () => UUID());
const couriers = Array.from({length: COURIERS}, () => UUID());
const pick = (list) => list[Math.floor(Math.random() * list.length)];
const start = new Date("2026-01-01T00:00:00Z").getTime();

function order() {
  const createdAt = new Date(start + Math.floor(Math.random() * 270 * 24 * 3600 * 1000));
  const status = pick(statuses);
  const items = Array.from({length: 1 + Math.floor(Math.random() * 3)}, () => {
    const quantity = 1 + Math.floor(Math.random() * 3);
    const unitPrice = 5 + Math.floor(Math.random() * 20);
    return {
      menuItemId: UUID(), name: pick(dishes), unitPrice: NumberDecimal(unitPrice + ".00"),
      quantity: quantity, lineTotal: NumberDecimal(unitPrice * quantity + ".00"),
    };
  });
  const total = items.reduce((sum, item) => sum + parseFloat(item.lineTotal.toString()), 0);
  const path = {CREATED: ["CREATED"], COOKING: ["CREATED", "COOKING"], READY: ["CREATED", "COOKING", "READY"],
    DELIVERED: ["CREATED", "COOKING", "READY", "IN_DELIVERY", "DELIVERED"], CANCELLED: ["CREATED", "CANCELLED"]}[status];
  const history = path.map((step, index) => ({
    status: step, actorType: index === 0 ? "SYSTEM" : "RESTAURANT", reason: step.toLowerCase(),
    createdAt: new Date(createdAt.getTime() + index * 60000),
    ...(index === 0 ? {} : {actorId: pick(customers)}),
  }));
  return {
    _id: UUID(), userId: pick(customers), restaurantId: pick(restaurants), restaurantName: "Restaurant",
    items: items, deliveryAddress: "Lenina 1", totalAmount: NumberDecimal(total.toFixed(2)), status: status,
    courierId: status === "DELIVERED" ? pick(couriers) : null, history: history,
    createdAt: createdAt, updatedAt: history[history.length - 1].createdAt,
  };
}

void db.orders.deleteMany({});
for (let done = 0; done < ORDERS; done += BATCH) {
  void db.orders.insertMany(Array.from({length: BATCH}, order), {ordered: false});
}
print("orders: " + db.orders.countDocuments());
