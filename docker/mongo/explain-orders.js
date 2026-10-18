// Compares the "my orders in status X" query without and with the compound indexes. Run after seed-orders.js:
//   docker compose cp docker/mongo/explain-orders.js mongo:/tmp/ && docker compose exec -T mongo mongosh --quiet delivery_explain --file /tmp/explain-orders.js
const compound = [
  {name: "orders_user_status_idx", owner: "userId"},
  {name: "orders_restaurant_status_idx", owner: "restaurantId"},
  {name: "orders_courier_status_idx", owner: "courierId"},
];
const createCompound = () => compound.forEach((index) =>
  db.orders.createIndex({[index.owner]: 1, status: 1, createdAt: -1, _id: -1}, {name: index.name}));
const dropCompound = () => compound.forEach((index) => {
  try { db.orders.dropIndex(index.name); } catch (e) { /* not created yet */ }
});

function stages(plan, found = []) {
  if (plan.stage) found.push(plan.stage);
  [plan.inputStage, ...(plan.inputStages || [])].filter(Boolean).forEach((child) => stages(child, found));
  return found;
}

function measure(owner, label) {
  const sample = db.orders.aggregate([
    {$match: {[owner]: {$ne: null}, status: {$in: ["DELIVERED", "CANCELLED"]}}},
    {$group: {_id: {[owner]: "$" + owner, status: "$status"}, n: {$sum: 1}}},
    {$sort: {n: -1}}, {$limit: 1},
  ]).toArray()[0]._id;
  const explain = db.orders.find(sample).sort({createdAt: -1, _id: -1}).explain("executionStats");
  const winning = explain.queryPlanner.winningPlan;
  const stats = explain.executionStats;
  print([label.padEnd(8), owner.padEnd(13), stages(winning).join(" > ").padEnd(34),
    "examined=" + stats.totalDocsExamined, "keys=" + stats.totalKeysExamined,
    "returned=" + stats.nReturned, "ms=" + stats.executionTimeMillis].join("  "));
}

const owners = compound.map((index) => index.owner);
print("documents: " + db.orders.countDocuments());
dropCompound();
owners.forEach((owner) => measure(owner, "before"));
createCompound();
owners.forEach((owner) => measure(owner, "after"));
