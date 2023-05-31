package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review addReview(Review review);

    Review findReviewById(Long reviewId);

    List<Review> getReviews(Long limitCount);

    Review updateReview(Review review);

    List<Review> getByFilmId(Long filmId, Long limitCount);

    void deleteReview(Long reviewId);

}
