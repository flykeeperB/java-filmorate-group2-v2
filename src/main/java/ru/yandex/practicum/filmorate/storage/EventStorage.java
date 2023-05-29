package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventStorage {

    List<Event> getEventById(long id);

    void addEventOnAddLike(long filmId, long userId);

    void addEventOnDeleteLike(long filmId, long userId);

    void addEventOnAddFriend(long userId, long friendId);

    void addEventOnDeleteFriend(long userId, long friendId);

    void addEventOnAddReview(long userId, long reviewId);

    void addEventOnDeleteReview(long userId, long reviewId);

    void addEventOnUpdateReview(long userId, long reviewId);

}
