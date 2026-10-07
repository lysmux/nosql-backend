--liquibase formatted sql

--changeset lysmux:create-users
create table users
(
    user_id              uuid primary key,
    name                 varchar(120) not null,
    email                varchar(254) not null,
    password_hash        text         not null,
    courier_mode_enabled boolean      not null default false,
    created_at           timestamptz  not null
);
create unique index users_email_key on users (lower(email));
--rollback drop table users;

--changeset lysmux:create-restaurants
create table restaurants
(
    restaurant_id uuid primary key,
    owner_id      uuid         not null references users (user_id),
    name          varchar(200) not null,
    created_at    timestamptz  not null
);
create index restaurants_owner_idx on restaurants (owner_id);
create index restaurants_catalog_idx on restaurants (name, restaurant_id);
--rollback drop table restaurants;

--changeset lysmux:create-menu-items
create table menu_items
(
    menu_item_id uuid primary key,
    restaurant_id uuid           not null references restaurants (restaurant_id),
    name          varchar(200)   not null,
    description   varchar(2000),
    price         numeric(12, 2) not null check (price >= 0),
    available     boolean        not null default true,
    archived      boolean        not null default false,
    created_at    timestamptz    not null,
    updated_at    timestamptz    not null
);
create index menu_items_list_idx on menu_items (restaurant_id, name, menu_item_id);
--rollback drop table menu_items;

--changeset lysmux:create-orders
create table orders
(
    order_id         uuid primary key,
    user_id          uuid           not null references users (user_id),
    restaurant_id    uuid           not null references restaurants (restaurant_id),
    restaurant_name  varchar(200)   not null,
    delivery_address varchar(500)   not null,
    total_amount     numeric(12, 2) not null check (total_amount >= 0),
    status           varchar(16)    not null check (status in
                                                    ('CREATED', 'COOKING', 'READY', 'IN_DELIVERY', 'DELIVERED', 'CANCELLED')),
    courier_id       uuid references users (user_id),
    created_at       timestamptz    not null,
    updated_at       timestamptz    not null
);
create index orders_list_idx on orders (created_at desc, order_id desc);
create index orders_user_idx on orders (user_id);
create index orders_restaurant_idx on orders (restaurant_id);
--rollback drop table orders;

--changeset lysmux:create-order-items
create table order_items
(
    order_item_id uuid primary key,
    order_id      uuid           not null references orders (order_id) on delete cascade,
    menu_item_id  uuid           not null,
    name          varchar(200)   not null,
    unit_price    numeric(12, 2) not null check (unit_price >= 0),
    quantity      int            not null check (quantity between 1 and 99),
    line_total    numeric(12, 2) not null check (line_total >= 0),
    constraint order_items_unique_menu_item unique (order_id, menu_item_id)
);
create index order_items_order_idx on order_items (order_id);
--rollback drop table order_items;

--changeset lysmux:create-order-status-history
create table order_status_history
(
    history_id  uuid primary key,
    order_id    uuid        not null references orders (order_id) on delete cascade,
    from_status varchar(16) check (from_status in
                                   ('CREATED', 'COOKING', 'READY', 'IN_DELIVERY', 'DELIVERED', 'CANCELLED')),
    to_status   varchar(16) not null check (to_status in
                                            ('CREATED', 'COOKING', 'READY', 'IN_DELIVERY', 'DELIVERED', 'CANCELLED')),
    actor_type  varchar(16) not null check (actor_type in ('USER', 'RESTAURANT', 'SYSTEM')),
    actor_id    uuid,
    reason      varchar(500) not null,
    created_at  timestamptz not null
);
create index order_status_history_order_idx on order_status_history (order_id, created_at);
--rollback drop table order_status_history;

--changeset lysmux:create-notifications
create table notifications
(
    notification_id uuid        primary key,
    user_id         uuid        not null references users (user_id),
    order_id        uuid        not null references orders (order_id),
    event_type      varchar(32) not null check (event_type in ('ORDER_CREATED', 'ORDER_STATUS_CHANGED')),
    status          varchar(16) not null check (status in
                                                ('CREATED', 'COOKING', 'READY', 'IN_DELIVERY', 'DELIVERED', 'CANCELLED')),
    text            text        not null,
    created_at      timestamptz not null
);
create index notifications_user_idx on notifications (user_id, created_at desc, notification_id desc);
--rollback drop table notifications;
