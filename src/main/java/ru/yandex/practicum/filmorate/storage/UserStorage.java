package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    //методы добавления, удаления и модификации объектов.
    User addUser(User user);

    User updateUser(long id, User user);

    List<User> findAllUser();

    User findUserById(long id);

    boolean contains(long id);

}
