package ru.yandex.practicum.filmorate.storage.database.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private static final String sqInsert = "INSERT INTO events " +
            "(time_event,user_id,type_id,operation_id,entity_id)" +
            " VALUES (?,?,?,?,?)";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Event> getEventById(long id) {
        List<Event> events = new ArrayList<>();
        String sqlQ = "SELECT event_id,time_event,user_id," +
                "type_name,operation_name,entity_id " +
                "FROM (((SELECT * FROM events WHERE user_id=?) AS e " +
                "LEFT JOIN event_type AS et " +
                "ON e.type_id=et.type_id) AS e_et " +
                "LEFT JOIN operation AS o " +
                "ON e_et.operation_id=o.operation_id)";

        SqlRowSet eventRows = jdbcTemplate.queryForRowSet("select 1 from events where user_id = ?", id);
        if (eventRows.next()) {
            events = jdbcTemplate.query(sqlQ, new EventMapper(), id);
        }
        return events;
    }

    @Override
    public void addEventOnAddLike(long filmId, long userId) {

        int type = getIdType("LIKE");
        int opr = getIdOperation("ADD");
        LocalDateTime date = LocalDateTime.now();

        jdbcTemplate.update(sqInsert, date, userId, type, opr, filmId);
        log.info("Событие на добавление лайка фильма {} от пользователя {} успешно добавлена", filmId, userId);
    }

    @Override
    public void addEventOnDeleteLike(long filmId, long userId) {

        int type = getIdType("LIKE");
        int opr = getIdOperation("REMOVE");
        LocalDateTime date = LocalDateTime.now();

        jdbcTemplate.update(sqInsert, date, userId, type, opr, filmId);
        log.info("Событие на удаление лайка фильма {} от пользователя {} успешно добавлена",
                filmId, userId);
    }

    @Override
    public void addEventOnAddFriend(long userId, long friendId) {

        int type = getIdType("FRIEND");
        int opr = getIdOperation("ADD");
        LocalDateTime date = LocalDateTime.now();

        jdbcTemplate.update(sqInsert, date, userId, type, opr, friendId);
        log.info("Событие на добавление в друзья пользователя {} пользователем {} успешно добавлена",
                friendId, userId);
    }

    @Override
    public void addEventOnDeleteFriend(long userId, long friendId) {

        int type = getIdType("FRIEND");
        int opr = getIdOperation("REMOVE");
        LocalDateTime date = LocalDateTime.now();

        jdbcTemplate.update(sqInsert, date, userId, type, opr, friendId);
        log.info("Событие на удаление из друзей пользователя {} пользователем {} успешно добавлена",
                friendId, userId);
    }

    @Override
    public void addEventOnAddReview(long userId, long reviewId) {

        int type = getIdType("REVIEW");
        int opr = getIdOperation("ADD");
        LocalDateTime date = LocalDateTime.now();

        jdbcTemplate.update(sqInsert, date, userId, type, opr, reviewId);
        log.info("Событие на добавление отзыва {} пользователем {} успешно добавлена",
                reviewId, userId);
    }

    @Override
    public void addEventOnDeleteReview(long userId, long reviewId) {

        int type = getIdType("REVIEW");
        int opr = getIdOperation("REMOVE");
        LocalDateTime date = LocalDateTime.now();

        jdbcTemplate.update(sqInsert, date, userId, type, opr, reviewId);
        log.info("Событие на удаление отзыва {} пользователем {} успешно добавлена",
                reviewId, userId);
    }

    @Override
    public void addEventOnUpdateReview(long userId, long reviewId) {

        int type = getIdType("REVIEW");
        int opr = getIdOperation("UPDATE");
        LocalDateTime date = LocalDateTime.now();

        jdbcTemplate.update(sqInsert, date, userId, type, opr, reviewId);
        log.info("Событие на обновление отзыва {} пользователем {} успешно добавлена",
                reviewId, userId);
    }

    private Integer getIdType(String nameType) {
        return jdbcTemplate.queryForObject("SELECT type_id FROM event_type " +
                "WHERE type_name = ?", Integer.class, nameType);
    }

    private Integer getIdOperation(String nameOperation) {
        return jdbcTemplate.queryForObject("SELECT operation_id FROM operation " +
                "WHERE operation_name = ?", Integer.class, nameOperation);
    }
}