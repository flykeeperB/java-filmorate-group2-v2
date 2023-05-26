package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.ReviewLikesStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewStorage reviewStorage;

    private final ReviewLikesStorage reviewLikesStorage;

    @Autowired
    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage,
                         @Qualifier("reviewLikesDbStorage") ReviewLikesStorage reviewLikesStorage) {
        this.reviewStorage = reviewStorage;
        this.reviewLikesStorage = reviewLikesStorage;
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
        return reviewStorage.add(review);
    }

    public void delete(Long id) {
        reviewStorage.delete(id);
    }

    public Review update(Review review) {
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
