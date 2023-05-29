package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.ReviewLikesStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;

    private final ReviewLikesStorage reviewLikesStorage;

    private final EventStorage eventStorage;

    @Autowired
    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
                         @Qualifier("reviewLikesDbStorage") ReviewLikesStorage reviewLikesStorage,
                         EventStorage eventStorage) {
        this.reviewStorage = reviewStorage;
        this.reviewLikesStorage = reviewLikesStorage;
        this.eventStorage = eventStorage;
    }

    public List<Review> getAll(Long limit) {
        return reviewStorage.getAll(limit);
    }

    public Review get(Long id) {
        return reviewStorage.get(id);
    }

    public List<Review> getByFilmId(Long id, Long limit) {
        return reviewStorage.getByFilmId(id, limit);
    }

    public Review create(Review review) {
        Review resultReview = reviewStorage.add(review);
        eventStorage.addEventOnAddReview(resultReview.getUserId(), resultReview.getReviewId());
        return resultReview;
    }

    public void delete(Long id) {
        Review resultReview = get(id);
        eventStorage.addEventOnDeleteReview(resultReview.getUserId(), resultReview.getReviewId());
        reviewStorage.delete(id);
    }

    public Review update(Review review) {
        Review resultReview = get(review.getReviewId());
        eventStorage.addEventOnUpdateReview(resultReview.getUserId(), resultReview.getReviewId());
        return reviewStorage.update(review);
    }

    public void addLike(long reviewId, long userId) {
        reviewLikesStorage.addLikeToReviewUseful(reviewId, userId);
    }

    public void deleteLike(long reviewId, long userId) {
        reviewLikesStorage.deleteLikeFromReviewUseful(reviewId, userId);
    }

    public void addDislike(long reviewId, long userId) {
        reviewLikesStorage.addDislikeToReviewUseful(reviewId, userId);
    }

    public void deleteDislike(long reviewId, long userId) {
        reviewLikesStorage.deleteDislikeFromReviewUseful(reviewId, userId);
    }
}
