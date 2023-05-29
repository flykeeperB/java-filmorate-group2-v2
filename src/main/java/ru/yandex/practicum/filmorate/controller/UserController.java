package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.ExtraFunctionalFilmService;
import ru.yandex.practicum.filmorate.service.ExtraFunctionalUserService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/users")
public class UserController {
    private final ExtraFunctionalUserService userService;
    private final ExtraFunctionalFilmService filmService;

    @Autowired
    public UserController(ExtraFunctionalUserService userService, ExtraFunctionalFilmService filmService) {
        this.userService = userService;
        this.filmService = filmService;
    }

    @GetMapping
    public List<User> findAll() {
        return userService.getListAllUsers();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> findListFriend(@PathVariable("id") Long userId) {
        return userService.getListFriends(userId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> findCommonFriends(@PathVariable("id") Long userId, @PathVariable("otherId") Long otherUserId) {
        return userService.getListCommonFriends(userId, otherUserId);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("POST request received: {}", user);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("User email empty");
            throw new ValidationException("Адрес электронной почты не может быть пустым.");
        }
        if (!user.getEmail().contains("@")) {
            log.error("User email does not contain the symbol @");
            throw new ValidationException("Адрес электронной почты должен содержать символ @.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("User login empty");
            throw new ValidationException("Логин не может быть пустым.");
        }
        if (user.getLogin().contains(" ")) {
            log.error("User login contains spaces");
            throw new ValidationException("Логин не может содержать пробелы.");
        }
        if (user.getName() == null || user.getName().isBlank() || user.getName().isEmpty()) {
            log.error("User name empty. Set login {} as name", user.getLogin());
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Birthday in the future");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        log.info("User added: {}", user);
        return userService.createUser(user);
    }

    @PutMapping
    public User put(@RequestBody User user) {
        log.info("PUT request received: {}", user);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("User email empty");
            throw new ValidationException("Адрес электронной почты не может быть пустым.");
        }
        if (!user.getEmail().contains("@")) {
            log.error("User email does not contain the symbol @");
            throw new ValidationException("Адрес электронной почты должен содержать символ @.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("User login empty");
            throw new ValidationException("Логин не может быть пустым.");
        }
        if (user.getLogin().contains(" ")) {
            log.error("User login contains spaces");
            throw new ValidationException("Логин не может содержать пробелы.");
        }
        if (user.getName() == null || user.getName().isBlank() || user.getName().isEmpty()) {
            log.error("User name empty. Set login {} as name", user.getLogin());
            user.setName(user.getLogin());
            System.out.println("Вместо имени используется логин, так как поле для имени осталось пустымю");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Birthday in the future");
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        return userService.updateUser(user.getId(), user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable("id") Long userId, @PathVariable("friendId") Long friendId) {
        userService.deleteFriend(userId, friendId);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(
            @PathVariable Long id) {
        userService.deleteUserById(id);
    }

    @GetMapping("/{id}/recommendations")
    public List<Film> getUserRecommendations(@PathVariable long id) {
        return filmService.getUserRecommendations(id);
    }


}