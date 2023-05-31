package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Test
    void createUser() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                        "}";
        User user = gson.fromJson(jsonUser, User.class);
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        user.setId(1);
        assertEquals(user, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldPostUserWithEmptyEmail() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldPostUserWithEmailWithoutSpecialSymbol() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \" \",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldPostUserWithEmptyLogin() {
        String jsonUser = "{" +
                "  \"login\": \"\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldPostUserWithLoginWithSpase() {
        String jsonUser = "{" +
                "  \"login\": \" \",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldPostUserWithNameEmptyAndGetNameEqualsLogin() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        user.setId(1);
        user.setName("dolore");
        assertEquals(user, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldPostUserWithBirthdayInFuture() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"2046-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        ResponseEntity<User> response = restTemplate.postForEntity("/users", user, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void getUserById() {
        String jsonUser1 = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user1 = gson.fromJson(jsonUser1, User.class);
        restTemplate.postForEntity("/users", user1, User.class);
        user1.setId(1);

        String jsonUser2 = "{" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"2000-01-01\"\n" +
                "}";
        User user2 = gson.fromJson(jsonUser2, User.class);
        restTemplate.postForEntity("/users", user2, User.class);
        user2.setId(2);

        ResponseEntity<User> response = restTemplate.getForEntity("/users/2", User.class);
        assertEquals(user2, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void getUserByNotExistId() {
        ResponseEntity<User> response = restTemplate.getForEntity("/users/2", User.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAllUser() {
        String jsonUser1 = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user1 = gson.fromJson(jsonUser1, User.class);
        restTemplate.postForEntity("/users", user1, User.class);
        user1.setId(1);

        String jsonUser2 = "{" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"2000-01-01\"\n" +
                "}";
        User user2 = gson.fromJson(jsonUser2, User.class);
        restTemplate.postForEntity("/users", user2, User.class);
        user2.setId(2);

        List<User> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        ResponseEntity<List<User>> response = restTemplate.exchange("/users", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        assertEquals(users, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getEmptyListAllUser() {
        ResponseEntity<List<User>> response = restTemplate.exchange("/users", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        List<User> users = new ArrayList<>();
        assertEquals(users, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());

    }

    @Test
    void shouldUser1AddFriendUser2() {
        String jsonUser1 = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user1 = gson.fromJson(jsonUser1, User.class);
        restTemplate.postForEntity("/users", user1, User.class);
        user1.setId(1);

        String jsonUser2 = "{" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"2000-01-01\"\n" +
                "}";
        User user2 = gson.fromJson(jsonUser2, User.class);
        restTemplate.postForEntity("/users", user2, User.class);
        user2.setId(2);

        List<User> friends = new ArrayList<>();
        friends.add(user2);

        HttpEntity<User> entity = new HttpEntity<>(user1);
        ResponseEntity<User> response = restTemplate.exchange("/users/1/friends/2", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseEntity<List<User>> response2 = restTemplate.exchange("/users/1/friends", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        assertEquals(friends, response2.getBody());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    void shouldUser1AddFriendNotExistUser2() {
        String jsonUser1 = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user1 = gson.fromJson(jsonUser1, User.class);
        restTemplate.postForEntity("/users", user1, User.class);
        user1.setId(1);

        List<User> friends = new ArrayList<>();

        HttpEntity<User> entity = new HttpEntity<>(user1);
        ResponseEntity<User> response = restTemplate.exchange("/users/1/friends/2", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        ResponseEntity<List<User>> response2 = restTemplate.exchange("/users/1/friends", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        assertEquals(friends, response2.getBody());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    void shouldNotExistUser2AddFriendUser1() {
        String jsonUser1 = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user1 = gson.fromJson(jsonUser1, User.class);
        restTemplate.postForEntity("/users", user1, User.class);
        user1.setId(1);

        HttpEntity<User> entity = new HttpEntity<>(user1);
        ResponseEntity<User> response = restTemplate.exchange("/users/2/friends/1", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCommonFriendUser1AndUser2() {
        String jsonUser1 = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user1 = gson.fromJson(jsonUser1, User.class);
        restTemplate.postForEntity("/users", user1, User.class);
        user1.setId(1);

        String jsonUser2 = "{" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"2000-01-01\"\n" +
                "}";
        User user2 = gson.fromJson(jsonUser2, User.class);
        restTemplate.postForEntity("/users", user2, User.class);
        user2.setId(2);

        String jsonCommonFriend = "{" +
                "  \"login\": \"friend\",\n" +
                "  \"name\": \"nameF\",\n" +
                "  \"email\": \"emailF@mail.ru\",\n" +
                "  \"birthday\": \"2001-01-05\"\n" +
                "}";
        User userCommonFriend = gson.fromJson(jsonCommonFriend, User.class);
        restTemplate.postForEntity("/users", userCommonFriend, User.class);
        userCommonFriend.setId(3);

        List<User> commonFriends = new ArrayList<>();
        commonFriends.add(userCommonFriend);

        HttpEntity<User> entity = new HttpEntity<>(user1);
        restTemplate.exchange("/users/1/friends/3", HttpMethod.PUT, entity, User.class);

        HttpEntity<User> entity2 = new HttpEntity<>(user2);
        restTemplate.exchange("/users/2/friends/3", HttpMethod.PUT, entity2, User.class);

        ResponseEntity<List<User>> response3 = restTemplate.exchange("/users/1/friends/common/2", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        assertEquals(commonFriends, response3.getBody());
        assertEquals(HttpStatus.OK, response3.getStatusCode());
    }

    @Test
    void getUserAfterUpdate() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        restTemplate.postForEntity("/users", user, User.class);
        user.setId(1);

        String jsonUpdateUser = "{" +
                "  \"id\": 1,\n" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-25\"\n" +
                "}";
        User updateUser = gson.fromJson(jsonUpdateUser, User.class);
        HttpEntity<User> entity = new HttpEntity<>(updateUser);
        ResponseEntity<User> response = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(updateUser, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldUserUpdateWithEmptyLogin() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        restTemplate.postForEntity("/users", user, User.class);
        user.setId(1);

        String jsonUpdateUser = "{" +
                "  \"id\": 1,\n" +
                "  \"login\": \"\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-25\"\n" +
                "}";
        User updateUser = gson.fromJson(jsonUpdateUser, User.class);
        HttpEntity<User> entity = new HttpEntity<>(updateUser);
        ResponseEntity<User> response = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldUserUpdateWithEmptyName() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        restTemplate.postForEntity("/users", user, User.class);
        user.setId(1);

        String jsonUpdateUser = "{" +
                "  \"id\": 1,\n" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-25\"\n" +
                "}";
        User updateUser = gson.fromJson(jsonUpdateUser, User.class);
        updateUser.setName("login");
        HttpEntity<User> entity = new HttpEntity<>(updateUser);
        ResponseEntity<User> response = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(updateUser, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void shouldUserUpdateWithEmptyEmail() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        restTemplate.postForEntity("/users", user, User.class);
        user.setId(1);

        String jsonUpdateUser = "{" +
                "  \"id\": 1,\n" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"\",\n" +
                "  \"birthday\": \"1946-08-25\"\n" +
                "}";
        User updateUser = gson.fromJson(jsonUpdateUser, User.class);
        HttpEntity<User> entity = new HttpEntity<>(updateUser);
        ResponseEntity<User> response = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void shouldUserUpdateWithBirthdayForFuture() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        restTemplate.postForEntity("/users", user, User.class);
        user.setId(1);

        String jsonUpdateUser = "{" +
                "  \"id\": 1,\n" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"2046-08-20\"\n" +
                "}";
        User updateUser = gson.fromJson(jsonUpdateUser, User.class);
        HttpEntity<User> entity = new HttpEntity<>(updateUser);
        ResponseEntity<User> response = restTemplate.exchange("/users", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteUser() {
        String jsonUser = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user1 = gson.fromJson(jsonUser, User.class);
        restTemplate.postForEntity("/users", user1, User.class);

        HttpEntity<User> entity = new HttpEntity<>(user1);
        ResponseEntity<User> response = restTemplate.exchange("/users/1", HttpMethod.DELETE, entity, User.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteNotExistUser() {
        ResponseEntity<User> response = restTemplate.exchange("/users/1", HttpMethod.DELETE, null,
                new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldUser1DeleteFriendUser2() {
        String jsonUser1 = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user1 = gson.fromJson(jsonUser1, User.class);
        restTemplate.postForEntity("/users", user1, User.class);

        String jsonUser2 = "{" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"2000-01-01\"\n" +
                "}";
        User user2 = gson.fromJson(jsonUser2, User.class);
        restTemplate.postForEntity("/users", user2, User.class);

        HttpEntity<User> entity = new HttpEntity<>(user1);
        ResponseEntity<User> response = restTemplate.exchange("/users/1/friends/2", HttpMethod.PUT, entity, User.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ResponseEntity<User> response2 = restTemplate.exchange("/users/1/friends/2", HttpMethod.DELETE, null,
                new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    void getRecommendation() {
        String jsonUser1 = "{" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user1 = gson.fromJson(jsonUser1, User.class);
        restTemplate.postForEntity("/users", user1, User.class);

        String jsonUser2 = "{" +
                "  \"login\": \"login\",\n" +
                "  \"name\": \"name\",\n" +
                "  \"email\": \"email@mail.ru\",\n" +
                "  \"birthday\": \"2000-01-01\"\n" +
                "}";
        User user2 = gson.fromJson(jsonUser2, User.class);
        restTemplate.postForEntity("/users", user2, User.class);

        String jsonFilm1 = "{" +
                "  \"name\": \"film\",\n" +
                "  \"description\": \"description\",\n" +
                "  \"releaseDate\": \"1946-08-20\",\n" +
                "  \"duration\": 180,\n" +
                "  \"mpa\": {\"id\": 1}\n" +
                "}";
        Film film1 = gson.fromJson(jsonFilm1, Film.class);
        restTemplate.postForEntity("/films", film1, Film.class);
        film1.setId(1);

        String jsonFilm2 = "{" +
                "  \"name\": \"film2\",\n" +
                "  \"description\": \"description\",\n" +
                "  \"releaseDate\": \"1946-08-20\",\n" +
                "  \"duration\": 180,\n" +
                "  \"mpa\": {\"id\": 1}\n" +
                "}";
        Film film2 = gson.fromJson(jsonFilm2, Film.class);
        restTemplate.postForEntity("/films", film2, Film.class);
        film2.setId(2);

        String jsonFilm3 = "{" +
                "  \"name\": \"film3\",\n" +
                "  \"description\": \"description\",\n" +
                "  \"releaseDate\": \"1946-08-20\",\n" +
                "  \"duration\": 180,\n" +
                "  \"mpa\": {\"id\": 1}\n" +
                "}";
        Film film3 = gson.fromJson(jsonFilm3, Film.class);
        restTemplate.postForEntity("/films", film3, Film.class);
        film3.setId(3);

        List<Film> recommendationFilms = new ArrayList<>();
        recommendationFilms.add(film3);

        HttpEntity<Film> entity = new HttpEntity<>(film1);
        ResponseEntity<Film> response = restTemplate.exchange("/films/1/like/1", HttpMethod.PUT, entity, Film.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        HttpEntity<Film> entity2 = new HttpEntity<>(film2);
        ResponseEntity<Film> response2 = restTemplate.exchange("/films/2/like/1", HttpMethod.PUT, entity2, Film.class);
        assertEquals(HttpStatus.OK, response2.getStatusCode());

        HttpEntity<Film> entity3 = new HttpEntity<>(film2);
        ResponseEntity<Film> response3 = restTemplate.exchange("/films/3/like/1", HttpMethod.PUT, entity3, Film.class);
        assertEquals(HttpStatus.OK, response3.getStatusCode());

        ResponseEntity<Film> response4 = restTemplate.exchange("/films/1/like/2", HttpMethod.PUT, entity, Film.class);
        assertEquals(HttpStatus.OK, response4.getStatusCode());

        ResponseEntity<Film> response5 = restTemplate.exchange("/films/2/like/2", HttpMethod.PUT, entity2, Film.class);
        assertEquals(HttpStatus.OK, response5.getStatusCode());

        ResponseEntity<List<Film>> response6 = restTemplate.exchange("/users/2/recommendations", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });
        assertEquals(recommendationFilms, response6.getBody());
        assertEquals(HttpStatus.OK, response6.getStatusCode());
    }

}
