package ru.yandex.practicum.filmorate.storage.database.reviews;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.storage.ReviewLikesStorage;

@Slf4j
@Repository("reviewLikesDbStorage")
public class ReviewLikesDbStorage implements ReviewLikesStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String ADD_EVAL_QUERY = "INSERT INTO LIKES_REVIEWS (" +
            "REVIEW_ID, USER_ID, EVAL) " +
            "VALUES (?, ?, ?)";

    private static final String UPDATE_EVAL_QUERY = "UPDATE LIKES_REVIEWS " +
            "SET EVAL = ?  WHERE " +
            "REVIEW_ID = ? AND USER_ID = ? ";

    private static final String DELETE_EVAL_QUERY = "DELETE FROM LIKES_REVIEWS WHERE " +
            "REVIEW_ID = ?   AND " +
            "USER_ID = ?     AND " +
            "EVAL = ?";

    private static final int LIKE = 1;
    private static final int DISLIKE = -1;

    private static final String UPDATE_USEFUL_QUERY = "UPDATE REVIEWS " +
            "SET REVIEWS.USEFUL = " +
            "(" +
            "SELECT SUM(EVAL) as SUMMED " +
            "FROM LIKES_REVIEWS " +
            "WHERE LIKES_REVIEWS.REVIEW_ID=?" +
            ") WHERE REVIEWS.REVIEW_ID = ?";

    @Autowired
    public ReviewLikesDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private void validateId(Long id) {
        if (id == null) {
            throw new ValidationException("Идентификатор не задан.");
        }
        if (id < 1) {
            throw new NotFoundException("Запись по неверному идентификатору не может быть найдена.");
        }
    }

    private void addEval(Long reviewId, Long userId, int eval) {
        try {
            jdbcTemplate.update(ADD_EVAL_QUERY, reviewId, userId, eval);
            log.info("Оценка (" + eval + ") отзыву добавлена (insert), userId=" + userId + " reviewID=" + reviewId);
        } catch (DataIntegrityViolationException e) {
            log.error(e.toString());
            try {
                jdbcTemplate.update(UPDATE_EVAL_QUERY, eval, reviewId, userId);
                log.info("Оценка (" + eval + ") отзыву добавлена (update), userId=" + userId + " reviewID=" + reviewId);
            } catch (RuntimeException eu) {
                log.error(eu.toString());
            }
        } catch (RuntimeException e) {
            log.error(e.toString());
        }
    }

    private void deleteEval(Long reviewId, Long userId, int eval) {
        try {
            int rows = jdbcTemplate.update(DELETE_EVAL_QUERY, reviewId, userId, eval);
            if (rows > 0) {
                log.info("Оценка (" + eval + ") отзыву удалена");
            } else {
                throw new NotFoundException("Запись не удалена.");
            }
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }
    }

    private void updateUsefulOfReview(Long reviewId) {
        try {
            jdbcTemplate.update(UPDATE_USEFUL_QUERY, reviewId, reviewId);
        } catch (DataAccessException e) {
            log.error(e.toString());
        }
    }

    private void addLike(Long reviewId, Long userId) {
        addEval(reviewId, userId, LIKE);
    }

    private void deleteLike(Long reviewId, Long userId) {
        deleteEval(reviewId, userId, LIKE);
    }

    private void addDislike(Long reviewId, Long userId) {
        addEval(reviewId, userId, DISLIKE);
    }

    private void deleteDislike(Long reviewId, Long userId) {
        deleteEval(reviewId, userId, DISLIKE);
    }

    @Override
    public void addLikeToReviewUseful(Long reviewId, Long userId) {
        validateId(reviewId);
        validateId(userId);
        addLike(reviewId, userId);
        updateUsefulOfReview(reviewId);
    }

    @Override
    public void deleteLikeFromReviewUseful(Long reviewId, Long userId) {
        validateId(reviewId);
        validateId(userId);
        deleteLike(reviewId, userId);
        updateUsefulOfReview(reviewId);
    }

    @Override
    public void addDislikeToReviewUseful(Long reviewId, Long userId) {
        validateId(reviewId);
        validateId(userId);
        addDislike(reviewId, userId);
        updateUsefulOfReview(reviewId);
    }

    @Override
    public void deleteDislikeFromReviewUseful(Long reviewId, Long userId) {
        validateId(reviewId);
        validateId(userId);
        deleteDislike(reviewId, userId);
        updateUsefulOfReview(reviewId);
    }
}
