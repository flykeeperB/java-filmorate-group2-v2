//package ru.yandex.practicum.filmorate;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import ru.yandex.practicum.filmorate.controller.UserController;
//import ru.yandex.practicum.filmorate.exceptions.ValidationException;
//import ru.yandex.practicum.filmorate.model.User;
//import ru.yandex.practicum.filmorate.service.UserService;
//import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
//
//public class UserControllerTest {
//    UserController userController;
//
//    User user;
//
//    @BeforeEach
//    void beforeEach() {
//        userController = new UserController(new UserService(new InMemoryUserStorage()));
//        user = new User();
//        user.setEmail("email@email.ru");
//        user.setLogin("Login");
//        user.setName("Name");
//        user.setBirthday(LocalDate.of(2000, 1, 1));
//
//    }
//
//    @Test
//    void shouldPostUserWithEmptyEmail() {
//        user.setEmail(" ");
//        final ValidationException exception = assertThrows(
//                ValidationException.class,
//                () -> userController.create(user));
//        assertEquals("Адрес электронной почты не может быть пустым.", exception.getMessage());
//    }
//
//    @Test
//    void shouldPostUserWithEmailWithoutSpecialSymbol() {
//        user.setEmail("email.ru");
//        final ValidationException exception = assertThrows(
//                ValidationException.class,
//                () -> userController.create(user));
//        assertEquals("Адрес электронной почты должен содержать символ @.", exception.getMessage());
//    }
//
//    @Test
//    void shouldPostUserWithEmptyLogin() {
//        user.setLogin(" ");
//        final ValidationException exception = assertThrows(
//                ValidationException.class,
//                () -> userController.create(user));
//        assertEquals("Логин не может быть пустым.", exception.getMessage());
//    }
//
//    @Test
//    void shouldPostUserWithLoginWithSpase() {
//        user.setLogin("Lo gin");
//        final ValidationException exception = assertThrows(
//                ValidationException.class,
//                () -> userController.create(user));
//        assertEquals("Логин не может содержать пробелы.", exception.getMessage());
//    }
//
//    @Test
//    void shouldPostUserWithNameEmptyAndGetNameEqualsLogin() {
//        user.setName("");
//        userController.create(user);
//
//        List<User> users = userController.findAll();
//        User actualUser = users.get(0);
//
//        assertEquals("Login", actualUser.getName());
//    }
//
//    @Test
//    void shouldPostUserWithBirthdayInFuture() {
//        user.setBirthday(LocalDate.now().plusDays(1));
//        final ValidationException exception = assertThrows(
//                ValidationException.class,
//                () -> userController.create(user));
//        assertEquals("Дата рождения не может быть в будущем.", exception.getMessage());
//    }
//}
