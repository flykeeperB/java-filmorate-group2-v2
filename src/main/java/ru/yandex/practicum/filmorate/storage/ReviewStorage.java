package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    Review add(Review review);

    Review get(Long id);

    List<Review> getAll(Long limitCount);

    Review update(Review review);

    List<Review> getByFilmId(Long id, Long limitCount);

    void delete(Long id);

}
