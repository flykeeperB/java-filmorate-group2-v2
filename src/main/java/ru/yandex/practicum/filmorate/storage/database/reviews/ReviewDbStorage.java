package ru.yandex.practicum.filmorate.storage.database.reviews;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.database.DbConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository("reviewDbStorage")
public class ReviewDbStorage implements ReviewStorage {

    private static final String SQL_REVIEWS_TABLE = "REVIEWS";
    private static final String SQL_REVIEWS_TABLE_ALIAS = "r";
    private static final String SQL_REVIEWS_TABLE_AND_ALIAS = SQL_REVIEWS_TABLE + " as " + SQL_REVIEWS_TABLE_ALIAS;
    private static final String SQL_REVIEWS_KEY_FIELD = "REVIEW_ID";

    private static final String SQL_UPDATE_REVIEW = "UPDATE " + SQL_REVIEWS_TABLE_AND_ALIAS + " SET " +
            SQL_REVIEWS_TABLE_ALIAS + ".CONTENT=:content, " +
            SQL_REVIEWS_TABLE_ALIAS + ".IS_POSITIVE=:isPositive " +
            "WHERE " + SQL_REVIEWS_TABLE_ALIAS + "." + SQL_REVIEWS_KEY_FIELD + "=:reviewId";

    private static final String SQL_DELETE_REVIEW = "DELETE FROM " +
            SQL_REVIEWS_TABLE_AND_ALIAS +
            " WHERE " + SQL_REVIEWS_TABLE_ALIAS + "." + SQL_REVIEWS_KEY_FIELD + "=:reviewId";

    protected static final String SQL_ORDER = "ORDER BY cast(" + SQL_REVIEWS_TABLE_ALIAS + ".USEFUL AS INT) DESC";
    protected static final String SQL_LIMIT = "LIMIT :limit";

    protected static final String SQL_GET_REVIEWS = "SELECT " +
            SQL_REVIEWS_TABLE_ALIAS + ".REVIEW_ID, " +
            SQL_REVIEWS_TABLE_ALIAS + ".USER_ID, " +
            SQL_REVIEWS_TABLE_ALIAS + ".FILM_ID, " +
            SQL_REVIEWS_TABLE_ALIAS + ".CONTENT, " +
            SQL_REVIEWS_TABLE_ALIAS + ".IS_POSITIVE, " +
            SQL_REVIEWS_TABLE_ALIAS + ".USEFUL " +
            "FROM " + SQL_REVIEWS_TABLE_AND_ALIAS;

    protected static final String SQL_GET_REVIEWS_FROM_IDS = SQL_GET_REVIEWS +
            " WHERE " + SQL_REVIEWS_TABLE_ALIAS + ".REVIEW_ID IN ( :ids ) " + SQL_ORDER;

    protected static final String SQL_GET_REVIEWS_BY_FILM_ID = SQL_GET_REVIEWS +
            " WHERE " + SQL_REVIEWS_TABLE_ALIAS + ".FILM_ID = :filmId " + SQL_ORDER + " " + SQL_LIMIT;

    private final DbConnector<Review> dbConnector;

    protected final UserStorage userStorage;

    protected final FilmStorage filmStorage;

    @Autowired
    public ReviewDbStorage(DbConnector<Review> dbConnector,
                           @Qualifier("userDbStorage") UserStorage userStorage,
                           @Qualifier("filmDbStorage") FilmStorage filmStorage
    ) {
        this.dbConnector = dbConnector;
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    @Override
    public Review addReview(Review review) {
        filmStorage.findFilmById(review.getFilmId());
        userStorage.findUserById(review.getUserId());

        final Map<String, Object> params = new HashMap<>();

        params.put("USER_ID", review.getUserId());
        params.put("FILM_ID", review.getFilmId());
        params.put("CONTENT", review.getContent());
        params.put("IS_POSITIVE", review.getIsPositive());
        params.put("USEFUL", 0);

        Long id = dbConnector.create(SQL_REVIEWS_TABLE,
                SQL_REVIEWS_KEY_FIELD,
                params);

        return findReviewById(id);
    }

    private List<Review> getReviews(List<Long> ids) {

        String query = SQL_GET_REVIEWS;
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (ids != null) {
            if (ids.isEmpty()) {
                return new ArrayList<>();
            } else {
                query = SQL_GET_REVIEWS_FROM_IDS;
            }
        }

        if (ids != null) {
            params.addValue("ids", ids);
        }

        return dbConnector.queryWithParams(query, params, new ReviewMapper());
    }

    @Override
    public Review findReviewById(Long reviewId) {
        DbConnector.validateId(reviewId);

        List<Review> reviews = getReviews(List.of(reviewId));

        if (reviews.isEmpty()) {
            throw new NotFoundException("Отзыва c таким id нет");
        }

        return reviews.get(0);
    }

    @Override
    public List<Review> getReviews(Long limit) {

        String query = SQL_GET_REVIEWS + " " + SQL_ORDER;

        if (limit != null) {

            query += " " + SQL_LIMIT;

        }

        MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);

        List<Long> ids = dbConnector.queryWithParamsByCustomType(query,
                params, (rs, numRow) ->
                        rs.getLong(SQL_REVIEWS_KEY_FIELD)
        );

        return getReviews(ids);
    }

    @Override
    public List<Review> getByFilmId(Long filmId, Long limit) {
        filmStorage.findFilmById(filmId);

        List<Long> ids = null;

        if (limit != null) {

            MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);
            params.addValue("filmId", filmId);

            ids = dbConnector.queryWithParamsByCustomType(
                    SQL_GET_REVIEWS_BY_FILM_ID,
                    params, (rs, numRow) -> rs.getLong("REVIEW_ID")
            );
        }

        return getReviews(ids);
    }

    @Override
    public Review updateReview(Review review) {
        findReviewById(review.getReviewId());

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("content", review.getContent());
        params.addValue("isPositive", review.getIsPositive());
        params.addValue("reviewId", review.getReviewId());

        if (dbConnector.runWithParams(SQL_UPDATE_REVIEW, params) != 0) {
            log.info("Запись успешно обновлена ");
        }

        return findReviewById(review.getReviewId());
    }

    @Override
    public void deleteReview(Long reviewId) {
        findReviewById(reviewId);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("reviewId", reviewId);

        if (dbConnector.runWithParams(SQL_DELETE_REVIEW, params) != 0) {
            log.info("Запись успешно удалена ");
        }
    }
}
