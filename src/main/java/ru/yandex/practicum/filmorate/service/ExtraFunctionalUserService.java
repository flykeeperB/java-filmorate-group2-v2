package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.database.users.ExtraFunctionalUserDbStorage;

import java.util.List;

@Service
public class ExtraFunctionalUserService extends UserService {
    private final ExtraFunctionalUserDbStorage userStorage;

    @Autowired
    public ExtraFunctionalUserService(@Qualifier("extraFunctionalUserDbStorage")
                                          ExtraFunctionalUserDbStorage userStorage) {
        super(userStorage);
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getListCommonFriends(long userId, long otherUserId) {
        return userStorage.findCommonFriends(userId, otherUserId);
    }

    public List<User> getListFriends(long userId) {
        return userStorage.findAllFriends(userId);
    }

}