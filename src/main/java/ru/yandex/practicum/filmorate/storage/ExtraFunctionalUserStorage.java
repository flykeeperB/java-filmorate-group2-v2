package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface ExtraFunctionalUserStorage extends UserStorage {

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    List<User> findAllFriends(long userId);

    List<User> findCommonFriends(long userId, long otherUserId);
}
