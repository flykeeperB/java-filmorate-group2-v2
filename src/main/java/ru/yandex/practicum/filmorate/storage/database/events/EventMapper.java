package ru.yandex.practicum.filmorate.storage.database.events;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Event;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {

        Event event = new Event();
        event.setEventId(rs.getLong("event_id"));
        event.setTimestamp(rs.getTimestamp("time_event").toInstant().toEpochMilli());
        event.setUserId(rs.getLong("user_id"));
        event.setEventType(rs.getString("type_name"));
        event.setOperation(rs.getString("operation_name"));
        event.setEntityId(rs.getLong("entity_id"));

        return event;
    }
}
