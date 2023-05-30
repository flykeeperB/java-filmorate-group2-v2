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
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("reviewDbStorage")
@Repository
public class ReviewDbStorage implements ReviewStorage {
    private static final String SQL_UPDATE_REVIEW = "UPDATE REVIEWS SET CONTENT=?, IS_POSITIVE=? WHERE FILM_ID=?";
    private static final String SQL_DELETE_REVIEW = "DELETE FROM REVIEWS WHERE REVIEW_ID=?";

    private final JdbcTemplate jdbcTemplate;

    private final ReviewMapper reviewMapper;


    @Autowired
    public ReviewDbStorage(JdbcTemplate jdbcTemplate, ReviewMapper reviewMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.reviewMapper = reviewMapper;
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
            log.info("Добавлен отзыв, id=" + newId);
            return get(newId.longValue());
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

        try {
            int status = jdbcTemplate.update(SQL_UPDATE_REVIEW,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getReviewId());

            if (status != 0) {
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

        try {
            int rows = jdbcTemplate.update(SQL_DELETE_REVIEW, id);
            if (rows > 0) {
                log.info("Запись удалена.");
            } else {
                throw new NotFoundException("Запись не удалена.");
            }
        } catch (RuntimeException e) {
            log.info(e.getMessage());
        }
    }
}
