package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.ReviewNotFoundException;
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
            throw new ReviewNotFoundException("Запись по неверному идентификатору не может быть найдена.");
        }
    }

    @Override
    public Review add(Review review) {
        validateId(review.getUserId());
        validateId(review.getFilmId());
        log.info("public Review add(Review review) "+review);
        SimpleJdbcInsert insertRequest = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("REVIEWS")
                .usingGeneratedKeyColumns("REVIEW_ID");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("USER_ID", review.getUserId());
        parameters.put("FILM_ID", review.getFilmId());
        parameters.put("CONTENT", review.getContent());
        parameters.put("IS_POSITIVE", review.getIsPositive());
        //USEFUL по умолчанию равен 0
        try {
            Number newId = insertRequest.executeAndReturnKey(parameters);
            log.info("Добавлен отзыв, id=" + newId.toString());
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
            throw new ReviewNotFoundException("Запись не найдена.");
        }
    }

    @Override
    public List<Review> getAll(Long limit) {
        String query = getBaseSelectSQL() +
                " ORDER BY USEFUL DESC";
        if (limit != null) {
            query += " LIMIT " + limit;
        }
        log.info(query);
        return jdbcTemplate.query(query, reviewMapper);
    }

    @Override
    public Review update(Review review) {
        validateId(review.getReviewId());
        String query = "UPDATE FILMS SET" +
                "CONTENT=?, " +
                "IS_POSITIVE=?, " +
                "WHERE FILM_ID=?";
        int status = jdbcTemplate.update(query,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        if (status != 0) {
            log.info("Запись успешно обновлена ");
        } else {
            throw new ReviewNotFoundException("Запись с заданным идентификатором для обновления не найдена.");
        }

        return get(review.getReviewId());
    }

    @Override
    public List<Review> getByFilmId(Long id, Long limitCount) {
        validateId(id);
        String query = getBaseSelectSQL() + " WHERE FILM_ID=?";
        if (limitCount != null) {
            query += " LIMIT " + limitCount;
        }
        try {
            return jdbcTemplate.query(query, reviewMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReviewNotFoundException("Записи не найдены.");
        }
    }

    @Override
    public void delete(Long id) {
        validateId(id);
        String query = "DELETE FROM REVIEWS WHERE id=?";
        int rows = jdbcTemplate.update(query, id);
        if (rows > 0) {
            log.info("Запись удалена.");
        } else {
            throw new ReviewNotFoundException("Запись не удалена.");
        }
    }
}
