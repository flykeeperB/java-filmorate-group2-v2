package ru.yandex.practicum.filmorate.storage.inmemory;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("InMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private int counterIdUser = 1;

    public long generateIdUser() {
        long id = counterIdUser++;
        return id;
    }

    @Override
    public User addUser(User user) {
        user.setId(generateIdUser());
        users.put(user.getId(), user);
        return users.get(user.getId());
    }

    @Override
    public User updateUser(long id, User user) {
        this.findUserById(id);
        users.put(id, user);
        return users.get(user.getId());
    }

    @Override
    public List<User> findAllUser() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findUserById(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(String.format("Пользователя с id %d нет.", id));
        }
        return users.get(id);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        findUserById(userId);
        findUserById(friendId);

        User user = users.get(userId);
        user.getFriends().add(friendId);
        users.put(user.getId(), user);

        User secondUser = users.get(friendId);
        secondUser.getFriends().add(userId);
        users.put(secondUser.getId(), secondUser);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        findUserById(userId);
        findUserById(friendId);

        User user = users.get(userId);
        user.getFriends().remove(friendId);
        users.put(user.getId(), user);

        User secobdUser = users.get(friendId);
        user.getFriends().remove(userId);
        users.put(secobdUser.getId(), secobdUser);
    }

    @Override
    public List<User> findAllFriends(long userId) {
        findUserById(userId);

        User user = users.get(userId);
        List<Long> listFriendsId = new ArrayList<>(user.getFriends());

        List<User> listFriends = new ArrayList<>();
        for (Long friendId : listFriendsId) {
            listFriends.add(users.get(friendId));
        }
        return listFriends;
    }

    @Override
    public List<User> findCommonFriends(long userId, long otherUserId) {
        findUserById(userId);
        findUserById(otherUserId);

        User firstUser = users.get(userId);
        List<Long> listFriendsIdFirstUser = new ArrayList<>(firstUser.getFriends());

        User secondUser = users.get(otherUserId);
        List<Long> listFriendsIdSecondUser = new ArrayList<>(secondUser.getFriends());

        List<User> listCommonFriends = new ArrayList<>();
        for (Long friendId : listFriendsIdFirstUser) {
            if (listFriendsIdSecondUser.contains(friendId)) {
                listCommonFriends.add(users.get(friendId));
            }
        }
        return listCommonFriends;
    }
}
