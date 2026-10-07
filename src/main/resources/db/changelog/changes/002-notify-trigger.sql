--liquibase formatted sql

--changeset lysmux:notify-notification-inserted splitStatements:false endDelimiter:
create function notify_notification_inserted() returns trigger as
$$
begin
    perform pg_notify(
        'order_notifications',
        json_build_object('notificationId', NEW.notification_id, 'userId', NEW.user_id)::text
    );
    return NEW;
end;
$$ language plpgsql;

create trigger notifications_notify_after_insert
    after insert on notifications
    for each row execute function notify_notification_inserted();
--rollback drop trigger notifications_notify_after_insert on notifications;
--rollback drop function notify_notification_inserted();
