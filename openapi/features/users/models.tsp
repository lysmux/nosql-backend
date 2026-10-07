namespace DeliveryService;

model User {
  userId: uuid;
  name: string;
  email: email;
  courierModeEnabled: boolean;
  createdAt: utcDateTime;
}
model CourierModeRequest { enabled: boolean; }
