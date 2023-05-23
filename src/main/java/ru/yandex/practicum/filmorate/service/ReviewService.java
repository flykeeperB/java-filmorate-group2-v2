package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewStorage storage;

    @Autowired
    public ReviewService(@Qualifier("reviewDbStorage") ReviewStorage reviewStorage) {
        this.storage = reviewStorage;
    }

    public List<Review> getAll(Long limit) {
        return storage.getAll(limit);
    }

    public Review get(Long id) {
        return storage.get(id);
    }

    public List<Review> getByFilmId(Long id, Long limit) {
        return storage.getByFilmId(id, limit);
    }

    public Review create(Review review) {
        return storage.add(review);
    }

    public void delete(Long id) {
        storage.delete(id);
    }

    public Review update(Review review) {
        return storage.update(review);
    }

    public void addLike(long reviewId, long userId) {
        //storage.addLike(filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        //storage.deleteLike(filmId, userId);
    }

    public void addDislike(long reviewId, long userId) {
        //storage.addDislike(filmId, userId);
    }

    public void deleteDislike(long filmId, long userId) {
        //storage.deleteDislike(filmId, userId);
    }
}
