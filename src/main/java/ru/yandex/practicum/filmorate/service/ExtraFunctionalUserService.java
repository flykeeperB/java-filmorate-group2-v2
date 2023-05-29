package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.ExtraFunctionalUserStorage;

import java.util.List;

@Service
public class ExtraFunctionalUserService extends UserService {
    private final ExtraFunctionalUserStorage userStorage;

    private final EventStorage eventStorage;

    @Autowired
    public ExtraFunctionalUserService(@Qualifier("extraFunctionalUserDbStorage")
                                      ExtraFunctionalUserStorage userStorage,
                                      EventStorage eventStorage) {
        super(userStorage);
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
    }

    public void addFriend(long userId, long friendId) {
        userStorage.addFriend(userId, friendId);
        eventStorage.addEventOnAddFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        userStorage.deleteFriend(userId, friendId);
        eventStorage.addEventOnDeleteFriend(userId, friendId);
    }

    public List<User> getListCommonFriends(long userId, long otherUserId) {
        return userStorage.findCommonFriends(userId, otherUserId);
    }

    public List<User> getListFriends(long userId) {
        return userStorage.findAllFriends(userId);
    }

}
