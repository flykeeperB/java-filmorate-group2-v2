package ru.yandex.practicum.filmorate.storage.database.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private static final String SQL_INSERT_EVENTS = "INSERT INTO EVENTS " +
            "(TIME_EVENT,USER_ID,TYPE_ID,OPERATION_ID,ENTITY_ID)" +
            " VALUES (?,?,?,?,?)";
    private static final String SQL_GET_OPERATION_ID = "SELECT OPERATION_ID FROM OPERATION WHERE OPERATION_NAME = ?";
    private static final String SQL_GET_TYPE_ID = "SELECT TYPE_ID FROM EVENT_TYPE WHERE TYPE_NAME = ?";
    private static String SQL_GET_EVENTS = "SELECT EVENT_ID, TIME_EVENT, USER_ID, " +
            "TYPE_NAME, OPERATION_NAME, ENTITY_ID " +
            "FROM (((SELECT * FROM EVENTS WHERE USER_ID=?) AS e " +
            "LEFT JOIN EVENT_TYPE AS et ON e.TYPE_ID = et.TYPE_ID) AS e_et " +
            "LEFT JOIN OPERATION AS o ON e_et.OPERATION_ID = o.OPERATION_ID)";
    private static String SQL_CHECK_EVENT = "SELECT 1 FROM EVENTS WHERE USER_ID = ?";
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Event> getEventById(long id) {
        List<Event> events = new ArrayList<>();
        SqlRowSet eventRows = jdbcTemplate.queryForRowSet(SQL_CHECK_EVENT, id);
        if (eventRows.next()) {
            events = jdbcTemplate.query(SQL_GET_EVENTS, new EventMapper(), id);
        }
        return events;
    }

    @Override
    public void addEventOnAddLike(long filmId, long userId) {
        addEvent("LIKE", "ADD", userId, filmId);
        log.info("Событие на добавление лайка фильма {} от пользователя {} успешно добавлено", filmId, userId);
    }

    @Override
    public void addEventOnDeleteLike(long filmId, long userId) {
        addEvent("LIKE", "REMOVE", userId, filmId);
        log.info("Событие на удаление лайка фильма {} от пользователя {} успешно добавлено",
                filmId, userId);
    }

    @Override
    public void addEventOnAddFriend(long userId, long friendId) {
        addEvent("FRIEND", "ADD", userId, friendId);
        log.info("Событие на добавление в друзья пользователя {} пользователем {} успешно добавлено",
                friendId, userId);
    }

    @Override
    public void addEventOnDeleteFriend(long userId, long friendId) {
        addEvent("FRIEND", "REMOVE", userId, friendId);
        log.info("Событие на удаление из друзей пользователя {} пользователем {} успешно добавлено",
                friendId, userId);
    }

    @Override
    public void addEventOnAddReview(long userId, long reviewId) {
        addEvent("REVIEW", "ADD", userId, reviewId);
        log.info("Событие на добавление отзыва {} пользователем {} успешно добавлено",
                reviewId, userId);
    }

    @Override
    public void addEventOnDeleteReview(long userId, long reviewId) {
        addEvent("REVIEW", "REMOVE", userId, reviewId);
        log.info("Событие на удаление отзыва {} пользователем {} успешно добавлено",
                reviewId, userId);
    }

    @Override
    public void addEventOnUpdateReview(long userId, long reviewId) {
        addEvent("REVIEW", "UPDATE", userId, reviewId);
        log.info("Событие на обновление отзыва {} пользователем {} успешно добавлено",
                reviewId, userId);
    }

    private Integer getIdType(String nameType) {
        return jdbcTemplate.queryForObject(SQL_GET_TYPE_ID, Integer.class, nameType);
    }

    private Integer getIdOperation(String nameOperation) {
        return jdbcTemplate.queryForObject(SQL_GET_OPERATION_ID, Integer.class, nameOperation);
    }

    private void addEvent(String nameType, String nameOperation, Long firstId, Long secondId) {
        int type = getIdType(nameType);
        int opr = getIdOperation(nameOperation);
        LocalDateTime date = LocalDateTime.now();
        jdbcTemplate.update(SQL_INSERT_EVENTS, date, firstId, type, opr, secondId);
    }
}