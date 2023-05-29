package ru.yandex.practicum.filmorate.storage.database.reviews;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("reviewDbStorage")
@Repository
public class ReviewDbStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    private final ReviewMapper reviewMapper;

    private final EventStorage eventStorage;

    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate, ReviewMapper reviewMapper, EventStorage eventStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewMapper = reviewMapper;
        this.eventStorage = eventStorage;
    }

    private String getBaseSelectSQL() {
        return "SELECT " +
                "REVIEW_ID, " +
                "USER_ID, " +
                "FILM_ID, " +
                "CONTENT, " +
                "IS_POSITIVE, " +
                "USEFUL " +
                "FROM REVIEWS";
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new ValidationException("Идентификатор не задан.");
        }
        if (id < 1) {
            throw new NotFoundException("Запись по неверному идентификатору не может быть найдена.");
        }
    }

    @Override
    public Review add(Review review) {
        validateId(review.getUserId());
        validateId(review.getFilmId());

        SimpleJdbcInsert insertRequest = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("REVIEWS")
                .usingGeneratedKeyColumns("REVIEW_ID");

        final Map<String, Object> parameters = new HashMap<>();

        parameters.put("USER_ID", review.getUserId());
        parameters.put("FILM_ID", review.getFilmId());
        parameters.put("CONTENT", review.getContent());
        parameters.put("IS_POSITIVE", review.getIsPositive());
        parameters.put("USEFUL", 0);

        try {
            Number newId = insertRequest.executeAndReturnKey(parameters);
            log.info("Добавлен отзыв, id=" + newId.toString());
            Review resultReview = get(newId.longValue());
            eventStorage.addReview(resultReview.getUserId(), resultReview.getReviewId());
            return resultReview;
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public Review get(Long id) {
        validateId(id);

        String query = getBaseSelectSQL() + " WHERE REVIEW_ID=?";

        try {
            return jdbcTemplate.queryForObject(query, reviewMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Запись не найдена.");
        }
    }

    @Override
    public List<Review> getAll(Long limit) {
        String query = getBaseSelectSQL() +
                " ORDER BY cast(USEFUL AS INT) DESC";

        if (limit != null) {
            query += " LIMIT " + limit;
        }

        return jdbcTemplate.query(query, reviewMapper);
    }

    @Override
    public Review update(Review review) {
        validateId(review.getReviewId());

        Review rev = get(review.getReviewId());

        String query = "UPDATE REVIEWS SET " +
                "CONTENT=?, " +
                "IS_POSITIVE=? " +
                "WHERE FILM_ID=?";

        try {
            int status = jdbcTemplate.update(query,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getReviewId());

            if (status != 0) {
                eventStorage.updateReview(rev.getUserId(), rev.getReviewId());
                log.info("Запись успешно обновлена ");
            } else {
                throw new NotFoundException("Запись с заданным идентификатором для обновления не найдена.");
            }
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }

        return get(review.getReviewId());
    }

    @Override
    public List<Review> getByFilmId(Long id, Long limitCount) {
        validateId(id);

        String query = getBaseSelectSQL() + " WHERE FILM_ID=?" +
                " ORDER BY USEFUL DESC";

        if (limitCount != null) {
            query += " LIMIT " + limitCount;
        }

        try {
            return jdbcTemplate.query(query, reviewMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Записи не найдены.");
        }
    }

    @Override
    public void delete(Long id) {
        validateId(id);

        String query = "DELETE FROM REVIEWS WHERE REVIEW_ID=?";

        Review review = get(id);

        try {
            int rows = jdbcTemplate.update(query, id);
            if (rows > 0) {
                eventStorage.deleteReview(review.getUserId(), review.getReviewId());
                log.info("Запись удалена.");
            } else {
                throw new NotFoundException("Запись не удалена.");
            }
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
    }
}
