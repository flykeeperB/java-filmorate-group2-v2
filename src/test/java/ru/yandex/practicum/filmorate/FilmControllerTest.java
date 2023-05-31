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
import ru.yandex.practicum.filmorate.model.Director;
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
public class FilmControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;
    Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    @Test
    void createIncorrectName() {
        Film film = new Film();
        film.setName(null);
        film.setDescription("exampleDescription");
        film.setReleaseDate(LocalDate.now().minusYears(10));
        film.setDuration(120);
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createIncorrectDescription() {
        Film film = new Film();
        film.setName("exampleName");
        film.setDescription("Пятеро друзей ( комик-группа «Шарло»), " +
                "приезжают в город Бризуль. Здесь они хотят разыскать господина Огюста Куглова, " +
                "который задолжал им деньги, а именно 20 миллионов. о Куглов, " +
                "который за время «своего отсутствия», стал кандидатом Коломбани.");
        film.setReleaseDate(LocalDate.now().minusYears(10));
        film.setDuration(120);
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void createIncorrectDuration() {
        Film film = new Film();
        film.setName("exampleName");
        film.setDescription("exampleDescription");
        film.setReleaseDate(LocalDate.now().minusYears(10));
        film.setDuration(-120);
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateIncorrectName() {
        Film film = new Film();
        film.setName("exampleName");
        film.setDescription("exampleDescription");
        film.setReleaseDate(LocalDate.now().minusYears(10));
        film.setDuration(120);
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        Film film2 = new Film();
        film.setName(null);
        film.setDescription("exampleDescription");
        film.setReleaseDate(LocalDate.now().minusYears(10));
        film.setDuration(120);
        HttpEntity<Film> entity = new HttpEntity<>(film2);
        ResponseEntity<Film> response2 = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    void updateIncorrectDescription() {
        Film film = new Film();
        film.setName("exampleName");
        film.setDescription("exampleDescription");
        film.setReleaseDate(LocalDate.now().minusYears(10));
        film.setDuration(120);
        restTemplate.postForLocation("/films", film);
        Film film2 = new Film();
        film.setName("exampleName");
        film.setDescription("Пятеро друзей ( комик-группа «Шарло»), " +
                "приезжают в город Бризуль. Здесь они хотят разыскать господина Огюста Куглова, " +
                "который задолжал им деньги, а именно 20 миллионов. о Куглов, " +
                "который за время «своего отсутствия», стал кандидатом Коломбани.");
        film.setReleaseDate(LocalDate.now().minusYears(10));
        film.setDuration(120);
        HttpEntity<Film> entity = new HttpEntity<>(film2);
        ResponseEntity<Film> response2 = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    void updateIncorrectDuration() {
        Film film = new Film();
        film.setName("exampleName");
        film.setDescription("exampleDescription");
        film.setReleaseDate(LocalDate.now().minusYears(10));
        film.setDuration(120);
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        Film film2 = new Film();
        film.setName("exampleName");
        film.setDescription("exampleDescription");
        film.setReleaseDate(LocalDate.now().minusYears(10));
        film.setDuration(-120);
        HttpEntity<Film> entity = new HttpEntity<>(film2);
        ResponseEntity<Film> response2 = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    void createFilm() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        ResponseEntity<Film> response = restTemplate.postForEntity("/films", film, Film.class);
        film.setId(1);
        assertEquals(film, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateFilm() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonFilm2 = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"nisis eiusmod\",\n" +
                "  \"description\": \"adipsisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film2 = gson.fromJson(jsonFilm2, Film.class);
        HttpEntity<Film> entity = new HttpEntity<>(film2);
        ResponseEntity<Film> response = restTemplate.exchange("/films", HttpMethod.PUT, entity, Film.class);
        assertEquals(film2, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void findAllFilms() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonFilm2 = "{\n" +
                "  \"name\": \"New film\",\n" +
                "  \"releaseDate\": \"1999-04-30\",\n" +
                "  \"description\": \"New film about friends\",\n" +
                "  \"duration\": 120,\n" +
                "  \"rate\": 4,\n" +
                "  \"mpa\": { \"id\": 3},\n" +
                "  \"genres\": [{ \"id\": 1}]\n" +
                "}";
        Film film2 = gson.fromJson(jsonFilm2, Film.class);
        restTemplate.postForEntity("/films", film2, Film.class);
        ResponseEntity<List<Film>> response = restTemplate.exchange("/films", HttpMethod.GET,
                null, new ParameterizedTypeReference<>() {
                });
        film.setId(1);
        film2.setId(2);
        List<Film> films = new ArrayList<>();
        films.add(film);
        films.add(film2);
        for (int i = 0; i < films.size(); i++) {
            assertEquals(films.get(i).getId(), response.getBody().get(i).getId());
        }
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void findFilmById() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        ResponseEntity<Film> response = restTemplate.getForEntity("/films/1", Film.class);
        film.setId(1);
        assertEquals(film, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getPopularFilm() {
        String jsonFilm = "{\n" +
                "  \"name\": \"New film\",\n" +
                "  \"releaseDate\": \"1999-04-30\",\n" +
                "  \"description\": \"New film about friends\",\n" +
                "  \"duration\": 120,\n" +
                "  \"mpa\": { \"id\": 3},\n" +
                "  \"genres\": [{ \"id\": 1}, { \"id\": 2}, { \"id\": 3}]\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonFilm2 = "{\n" +
                "  \"name\": \"Film Updated\",\n" +
                "  \"releaseDate\": \"1999-04-17\",\n" +
                "  \"description\": \"New film update decription\",\n" +
                "  \"duration\": 190,\n" +
                "  \"rate\": 4,\n" +
                "  \"mpa\": { \"id\": 5},\n" +
                "  \"genres\": []\n" +
                "}";
        Film film2 = gson.fromJson(jsonFilm2, Film.class);
        restTemplate.postForEntity("/films", film2, Film.class);
        ResponseEntity<List<Film>> response = restTemplate.exchange("/films/popular?year=1999&genreId=1",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
        film.setId(1);
        film2.setId(2);
        List<Film> films = new ArrayList<>();
        films.add(film);
        for (int i = 0; i < films.size(); i++) {
            assertEquals(films.get(i).getId(), response.getBody().get(i).getId());
        }

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addLikeToFilm() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        restTemplate.postForEntity("/users", user, User.class);
        ResponseEntity<Film> response = restTemplate.exchange("/films/1/like/1", HttpMethod.PUT,
                null, Film.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteLikeToFilm() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        restTemplate.postForEntity("/users", user, User.class);
        ResponseEntity<Film> responseAdd = restTemplate
                .exchange("/films/1/like/1", HttpMethod.PUT, null, Film.class);
        assertEquals(HttpStatus.OK, responseAdd.getStatusCode());
        ResponseEntity<Film> responseDelete = restTemplate
                .exchange("/films/1/like/1", HttpMethod.DELETE, null, Film.class);
        assertEquals(HttpStatus.OK, responseDelete.getStatusCode());
    }

    @Test
    void searchFilmByTitle() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonFilm2 = "{\n" +
                "  \"name\": \"Film Updated\",\n" +
                "  \"releaseDate\": \"1999-04-17\",\n" +
                "  \"description\": \"New film update decription\",\n" +
                "  \"duration\": 190,\n" +
                "  \"rate\": 4,\n" +
                "  \"mpa\": { \"id\": 5},\n" +
                "  \"genres\": []\n" +
                "}";
        Film film2 = gson.fromJson(jsonFilm2, Film.class);
        restTemplate.postForEntity("/films", film2, Film.class);
        film.setId(1);
        List<Film> films = new ArrayList<>();
        films.add(film);
        ResponseEntity<List<Film>> responseSearch = restTemplate.exchange("/films/search?query=nisi&by=title",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, responseSearch.getStatusCode());
        for (int i = 0; i < films.size(); i++) {
            assertEquals(films.get(i).getId(), responseSearch.getBody().get(i).getId());
        }
    }

    @Test
    void searchFilmByDirector() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonDirector = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Temp Director\"\n" +
                "}";
        Director director = gson.fromJson(jsonDirector, Director.class);
        restTemplate.postForEntity("/directors", director, Director.class);
        String jsonFilm2 = "{\n" +
                "  \"name\": \"New film with director\",\n" +
                "  \"releaseDate\": \"1999-04-30\",\n" +
                "  \"description\": \"Film with director\",\n" +
                "  \"duration\": 120,\n" +
                "  \"mpa\": { \"id\": 3},\n" +
                "  \"genres\": [{ \"id\": 1}],\n" +
                "  \"directors\": [{ \"id\": 1}]\n" +
                "}";
        Film film2 = gson.fromJson(jsonFilm2, Film.class);
        restTemplate.postForEntity("/films", film2, Film.class);
        film2.setId(2);
        List<Film> films = new ArrayList<>();
        films.add(film2);
        ResponseEntity<List<Film>> responseSearch = restTemplate.exchange("/films/search?query=tem&by=director",
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {
                });
        assertEquals(HttpStatus.OK, responseSearch.getStatusCode());
        for (int i = 0; i < films.size(); i++) {
            assertEquals(films.get(i).getId(), responseSearch.getBody().get(i).getId());
        }
    }

    @Test
    void getCommonFilm() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonFilm2 = "{\n" +
                "  \"name\": \"New film with director\",\n" +
                "  \"releaseDate\": \"1999-04-30\",\n" +
                "  \"description\": \"Film with director\",\n" +
                "  \"duration\": 120,\n" +
                "  \"mpa\": { \"id\": 3},\n" +
                "  \"genres\": [{ \"id\": 1}],\n" +
                "  \"directors\": [{ \"id\": 1}]\n" +
                "}";
        Film film2 = gson.fromJson(jsonFilm2, Film.class);
        restTemplate.postForEntity("/films", film2, Film.class);
        String jsonUser = "{\n" +
                "  \"login\": \"dolore\",\n" +
                "  \"name\": \"Nick Name\",\n" +
                "  \"email\": \"mail@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User user = gson.fromJson(jsonUser, User.class);
        restTemplate.postForEntity("/users", user, User.class);
        String jsonFriend = "{\n" +
                "  \"login\": \"dolores\",\n" +
                "  \"name\": \"Nick Names\",\n" +
                "  \"email\": \"mails@mail.ru\",\n" +
                "  \"birthday\": \"1946-08-20\"\n" +
                "}";
        User friend = gson.fromJson(jsonFriend, User.class);
        restTemplate.postForEntity("/users", friend, User.class);
        ResponseEntity<Film> response = restTemplate
                .exchange("/films/1/like/1", HttpMethod.PUT, null, Film.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseEntity<Film> response2 = restTemplate
                .exchange("/films/1/like/2", HttpMethod.PUT, null, Film.class);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        ResponseEntity<List<Film>> responseCommon = restTemplate
                .exchange("/films/common?userId=1&friendId=2", HttpMethod.GET,
                        null, new ParameterizedTypeReference<>() {
                        });
        film.setId(1);
        List<Film> films = new ArrayList<>();
        films.add(film);
        for (int i = 0; i < films.size(); i++) {
            assertEquals(films.get(i).getId(), responseCommon.getBody().get(i).getId());
        }
        assertEquals(HttpStatus.OK, responseCommon.getStatusCode());
    }

    @Test
    void getFilmByDirectorByYear() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonDirector = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Temp Director\"\n" +
                "}";
        Director director = gson.fromJson(jsonDirector, Director.class);
        restTemplate.postForEntity("/directors", director, Director.class);
        String jsonFilm2 = "{\n" +
                "  \"name\": \"New film with director\",\n" +
                "  \"releaseDate\": \"1999-04-30\",\n" +
                "  \"description\": \"Film with director\",\n" +
                "  \"duration\": 120,\n" +
                "  \"mpa\": { \"id\": 3},\n" +
                "  \"genres\": [{ \"id\": 1}],\n" +
                "  \"directors\": [{ \"id\": 1}]\n" +
                "}";
        Film film2 = gson.fromJson(jsonFilm2, Film.class);
        restTemplate.postForEntity("/films", film2, Film.class);
        film2.setId(2);
        List<Film> films = new ArrayList<>();
        films.add(film2);
        ResponseEntity<List<Film>> responseSearch = restTemplate
                .exchange("/films/director/1?sortBy=year", HttpMethod.GET,
                        null, new ParameterizedTypeReference<>() {
                        });
        assertEquals(HttpStatus.OK, responseSearch.getStatusCode());
        for (int i = 0; i < films.size(); i++) {
            assertEquals(films.get(i).getId(), responseSearch.getBody().get(i).getId());
        }
    }

    @Test
    void getFilmByDirectorByLike() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        String jsonDirector = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"Temp Director\"\n" +
                "}";
        Director director = gson.fromJson(jsonDirector, Director.class);
        restTemplate.postForEntity("/directors", director, Director.class);
        String jsonFilm2 = "{\n" +
                "  \"name\": \"New film with director\",\n" +
                "  \"releaseDate\": \"1999-04-30\",\n" +
                "  \"description\": \"Film with director\",\n" +
                "  \"duration\": 120,\n" +
                "  \"mpa\": { \"id\": 3},\n" +
                "  \"genres\": [{ \"id\": 1}],\n" +
                "  \"directors\": [{ \"id\": 1}]\n" +
                "}";
        Film film2 = gson.fromJson(jsonFilm2, Film.class);
        restTemplate.postForEntity("/films", film2, Film.class);
        film2.setId(2);
        List<Film> films = new ArrayList<>();
        films.add(film2);
        ResponseEntity<List<Film>> responseSearch = restTemplate
                .exchange("/films/director/1?sortBy=likes", HttpMethod.GET,
                        null, new ParameterizedTypeReference<>() {
                        });
        assertEquals(HttpStatus.OK, responseSearch.getStatusCode());
        for (int i = 0; i < films.size(); i++) {
            assertEquals(films.get(i).getId(), responseSearch.getBody().get(i).getId());
        }
    }

    @Test
    void deleteFilmById() {
        String jsonFilm = "{" +
                "  \"name\": \"nisi eiusmod\",\n" +
                "  \"description\": \"adipisicing\",\n" +
                "  \"releaseDate\": \"1967-03-25\",\n" +
                "  \"duration\": 100,\n" +
                "  \"mpa\": { \"id\": 1}\n" +
                "}";
        Film film = gson.fromJson(jsonFilm, Film.class);
        restTemplate.postForEntity("/films", film, Film.class);
        ResponseEntity<Film> response = restTemplate.getForEntity("/films/1", Film.class);
        film.setId(1);
        assertEquals(film, response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseEntity<Film> responseDelete = restTemplate
                .exchange("/films/1", HttpMethod.DELETE, null, Film.class);
        assertEquals(HttpStatus.OK, responseDelete.getStatusCode());
    }
}