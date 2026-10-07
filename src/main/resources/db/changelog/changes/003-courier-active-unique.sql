--liquibase formatted sql

--changeset lysmux:courier-one-active-order
create unique index orders_one_active_per_courier on orders (courier_id) where status in ('READY', 'IN_DELIVERY');
--rollback drop index orders_one_active_per_courier;
