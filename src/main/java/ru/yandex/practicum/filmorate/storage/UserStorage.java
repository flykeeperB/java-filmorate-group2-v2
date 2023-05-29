package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    //методы добавления, удаления и модификации объектов.
    User addUser(User user);

    User updateUser(Long id, User user);

    List<User> findAllUser();

    List<User> getUsers(List<Long> ids);

    User findUserById(Long id);

    void deleteUserById(Long userId);
}
