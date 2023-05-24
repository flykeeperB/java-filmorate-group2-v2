package ru.yandex.practicum.filmorate.storage;

public interface ReviewLikesStorage {

    void addLikeToReviewUseful(Long reviewId, Long userId);

    void deleteLikeFromReviewUseful(Long reviewId, Long userId);

    void addDislikeToReviewUseful(Long reviewId, Long userId);

    void deleteDislikeFromReviewUseful(Long reviewId, Long userId);

}
