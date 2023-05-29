package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> getListAllUsers() {
        return userStorage.findAllUser();
    }

    public User createUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(long id, User user) {
        return userStorage.updateUser(id, user);
    }

    public User getUserById(long id) {
        return userStorage.findUserById(id);
    }

    public void deleteUserById(long userId) {
        userStorage.deleteUserById(userId);
    }

}
