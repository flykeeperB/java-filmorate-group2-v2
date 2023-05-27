package ru.yandex.practicum.filmorate.dao;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.FilmWithSearchStorage;

import java.util.*;

import static ru.yandex.practicum.filmorate.dao.UserDbStorage.FIND_USER_BY_ID_IN_TABLE_SQL;

//класс DAO - объект доступа к данным. Необходимо написать
// соответствующие мапперы и методы, позволяющие сохранять
// фильмы в БД и получать их из неё

@Component("filmDbStorage")
@Repository
@AllArgsConstructor
public class FilmDbStorage implements FilmWithSearchStorage {
    static final String FIND_FILM_BY_ID_IN_TABLE_SQL = "SELECT * FROM FILMS WHERE FILM_ID=?";
    static final String GET_FILMS_FROM_TABLE_SQL = "SELECT f.*, l.GENRE_ID, l.GENRE_NAME, m.MPA_NAME "
            + "FROM FILMS AS f LEFT JOIN GENRES AS g ON f.FILM_ID = g.FILM_ID "
            + "LEFT JOIN LIST_OF_GENRES AS l ON g.GENRE_ID = l.GENRE_ID "
            + "LEFT JOIN LIST_OF_MPAS AS m on f.MPA_ID = m.MPA_ID ";
    private final JdbcTemplate jdbcTemplate;

    private final EventStorage eventStorage;


    private void addGenres(long idGenre, long idFilm) {
        try {
            String sqlQuery = "INSERT INTO GENRES (FILM_ID, GENRE_ID) VALUES (?,?)";

            jdbcTemplate.update(sqlQuery, idFilm, idGenre);
        } catch (DataIntegrityViolationException e) {
            return;
        }
    }

    private void deleteGenre(long idFilm) {
        String sqlQuery = "DELETE FROM GENRES WHERE FILM_ID=?";

        jdbcTemplate.update(sqlQuery, idFilm);
    }

    private void addDirectors(long idDirector, long idFilm) {
        try {
            String sqlQuery = "INSERT INTO DIRECTORS (DIRECTOR_ID, FILM_ID) VALUES (?,?)";

            jdbcTemplate.update(sqlQuery, idDirector, idFilm);
        } catch (DataIntegrityViolationException e) {
            return;
        }
    }

    private void deleteDirectors(long idFilm) {
        String sqlQuery = "DELETE FROM DIRECTORS WHERE FILM_ID=?";

        jdbcTemplate.update(sqlQuery, idFilm);
    }

    private List<Genre> getListGenresForFilm(long idFilm) {
        List<Genre> genres = jdbcTemplate.query("SELECT GENRE_ID FROM GENRES WHERE FILM_ID=" + idFilm, new GenreExtractor());

        return genres;
    }

    @Override
    public Film addFilm(Film film) {
        SimpleJdbcInsert insertIntoFilm = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("FILMS")
                .usingGeneratedKeyColumns("FILM_ID");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("FILM_NAME", film.getName());
        parameters.put("DESCRIPTION", film.getDescription());
        parameters.put("RELEASE_DATE", film.getReleaseDate());
        parameters.put("DURATION", film.getDuration());
        parameters.put("MPA_ID", film.getMpa().getId());
        Number newId = insertIntoFilm.executeAndReturnKey(parameters);
        film.setId(newId.longValue());

        Set<Genre> genres = film.getGenres();
        if (!genres.equals(new HashSet<>())) {
            for (Genre genre : genres) {
                addGenres(genre.getId(), film.getId());
            }
        }

        Set<Director> directors = film.getDirectors();
        if (!directors.equals(new HashSet<>())) {
            for (Director director : directors) {
                addDirectors(director.getId(), film.getId());
            }
        }

        return film;
    }

    @Override
    public Film updateFilm(long filmId, Film film) {
        Film filmExist = jdbcTemplate.query(FIND_FILM_BY_ID_IN_TABLE_SQL,
                new Object[]{filmId}, new FilmMapper()).stream().findAny().orElse(null);
        if (filmExist == null) {
            throw new FilmNotFoundException("Фильма c таким id нет");
        } else {
            String sqlQuery = "UPDATE FILMS SET FILM_NAME=?, DESCRIPTION=?, RELEASE_DATE=?, DURATION=?, MPA_ID=? "
                    + "WHERE FILM_ID=?";

            jdbcTemplate.update(
                    sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    filmId);

            deleteGenre(film.getId());

            Set<Genre> genres = film.getGenres();
            if (!genres.equals(new HashSet<>())) {
                for (Genre genre : genres) {
                    addGenres(genre.getId(), film.getId());
                }
            }

            deleteDirectors(film.getId());

            Set<Director> directors = film.getDirectors();
            if (!directors.equals(new HashSet<>())) {
                for (Director director : directors) {
                    addDirectors(director.getId(), film.getId());
                }
            }
        }
        return film;
    }

    @Override
    public List<Film> findAllFilms() {
        Map<Film, List<Genre>> filmsWithGenre = jdbcTemplate.query(GET_FILMS_FROM_TABLE_SQL, new FilmExtractor());

        String sqlDirectors = "SELECT d.FILM_ID,d.DIRECTOR_ID,l.DIRECTOR_NAME FROM LIST_OF_DIRECTORS AS l "
                + "LEFT JOIN DIRECTORS AS d ON d.DIRECTOR_ID = l.DIRECTOR_ID";

        Map<Long, Set<Director>> directorsAndFilms = jdbcTemplate.query(sqlDirectors, new DirectorExtractor());

        List<Film> films = new ArrayList<>();
        for (Film film : filmsWithGenre.keySet()) {
            Set<Genre> genres = new HashSet<>();
            for (Genre genre : filmsWithGenre.get(film)) {
                if (genre.getId() != 0) {
                    genres.add(genre);
                }
            }
            film.setGenres(genres);

            Set<Director> directors = new HashSet<>();
            if (directorsAndFilms.containsKey(film.getId())) {
                directors = directorsAndFilms.get(film.getId());
            }
            film.setDirectors(directors);

            films.add(film);
        }

        return films;
    }

    @Override
    public Film findFilmById(long filmId) {
        Film film = new Film();
        Film filmExist = jdbcTemplate.query(FIND_FILM_BY_ID_IN_TABLE_SQL,
                new Object[]{filmId}, new FilmMapper()).stream().findAny().orElse(null);
        if (filmExist == null) {
            throw new FilmNotFoundException("Фильма c таким id нет");
        } else {
            Map<Film, List<Genre>> filmWithGenre = jdbcTemplate.query(
                    GET_FILMS_FROM_TABLE_SQL
                            + "WHERE f.FILM_ID=?;", new Object[]{filmId}, new FilmExtractor());

            String sqlDirectors = "SELECT d.FILM_ID,d.DIRECTOR_ID,l.DIRECTOR_NAME FROM LIST_OF_DIRECTORS AS l "
                    + "LEFT JOIN DIRECTORS AS d ON d.DIRECTOR_ID = l.DIRECTOR_ID";

            Map<Long, Set<Director>> directorsAndFilms = jdbcTemplate.query(sqlDirectors, new DirectorExtractor());

            for (Film f : filmWithGenre.keySet()) {
                Set<Genre> genres = new HashSet<>();
                for (Genre genre : filmWithGenre.get(f)) {
                    if (genre.getId() != 0) {
                        genres.add(genre);
                    }
                }
                f.setGenres(genres);

                Set<Director> directors = new HashSet<>();
                if (directorsAndFilms.containsKey(filmId)) {
                    directors = directorsAndFilms.get(filmId);
                }
                f.setDirectors(directors);

                film = f;
            }
            return film;
        }
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        Map<Film, List<Genre>> filmsWithGenre = jdbcTemplate.query(
                GET_FILMS_FROM_TABLE_SQL
                        + "LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS COUNT_LIKES FROM LIKES GROUP BY FILM_ID) AS likes "
                        + "ON f.FILM_ID=likes.FILM_ID ORDER BY likes.COUNT_LIKES DESC LIMIT "
                        + count, new FilmExtractor());
        List<Film> films = new ArrayList<>();
        for (Film film : filmsWithGenre.keySet()) {
            Set<Genre> genres = new HashSet<>();
            for (Genre genre : filmsWithGenre.get(film)) {
                if (genre.getId() != 0) {
                    genres.add(genre);
                }
            }
            film.setGenres(genres);

            films.add(film);
        }
        return films;

    }

    @Override
    public void deleteLike(long filmId, long userId) {
        User userExist = jdbcTemplate.query(FIND_USER_BY_ID_IN_TABLE_SQL,
                new Object[]{userId}, new UserMapper()).stream().findAny().orElse(null);
        if (userExist == null) {
            throw new UserNotFoundException("Такого пользователя нет");
        } else {
            String sqlQuery = "DELETE FROM LIKES WHERE FILM_ID=? AND USER_ID=?";
            jdbcTemplate.update(sqlQuery, filmId, userId);
            eventStorage.deleteLike(filmId, userId);
        }
    }

    @Override
    public void addLike(long filmId, long userId) {
        String sql = "INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        eventStorage.addLike(filmId, userId);
    }

    @Override
    public List<Film> searchFilms(String query, String by) {
        String[] byArr = by.split(",");
        StringBuilder sqlFilms = new StringBuilder(GET_FILMS_FROM_TABLE_SQL +
                "LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS COUNT_LIKES FROM LIKES GROUP BY FILM_ID) AS likes " +
                "ON f.FILM_ID=likes.FILM_ID WHERE ");
        StringBuilder sqlDirectors = new StringBuilder("SELECT d.FILM_ID,d.DIRECTOR_ID,l.DIRECTOR_NAME " +
                "FROM LIST_OF_DIRECTORS AS l LEFT JOIN DIRECTORS AS d ON d.DIRECTOR_ID = l.DIRECTOR_ID WHERE ");
        for (int i = 0; i < byArr.length; i++) {
            if (byArr[i].equals("title")) {
                sqlFilms.append("upper(FILM_NAME) LIKE '%").append(query.toUpperCase()).append("%'");
                sqlDirectors.append("d.FILM_ID IN (SELECT FILM_ID FROM FIlMS " +
                        "WHERE upper(FILM_NAME) LIKE '%").append(query.toUpperCase()).append("%')");
            } else if (byArr[i].equals("director")) {
                sqlFilms.append("f.FILM_ID IN " + "(SELECT FILM_ID FROM DIRECTORS WHERE DIRECTOR_ID IN " +
                        "(SELECT DIRECTOR_ID FROM LIST_OF_DIRECTORS WHERE upper(DIRECTOR_NAME) " +
                        "LIKE '%").append(query.toUpperCase()).append("%')) ");
                sqlDirectors.append("upper(DIRECTOR_NAME) LIKE '%").append(query.toUpperCase()).append("%'");
            }
            if (i != byArr.length - 1) {
                sqlDirectors.append(" OR ");
                sqlFilms.append("OR ");
            }
        }
        sqlFilms.append("ORDER BY likes.COUNT_LIKES DESC ");
        Map<Long, Map<Film, List<Genre>>> sortFilmsWithGenre = jdbcTemplate.query(sqlFilms.toString(), (rs) -> {
            Map<Long, Map<Film, List<Genre>>> sortFilms = new HashMap<>();
            Long sortCount = 1L;
            while (rs.next()) {
                Map<Film, List<Genre>> data = new HashMap<>();
                Film film = new Film();
                film.setId(rs.getInt("FILM_ID"));
                film.setName(rs.getString("FILM_NAME"));
                film.setDescription(rs.getString("DESCRIPTION"));
                film.setReleaseDate(rs.getDate("RELEASE_DATE").toLocalDate());
                film.setDuration(rs.getInt("DURATION"));

                Mpa mpa = new Mpa();
                mpa.setId(rs.getInt("MPA_ID"));
                mpa.setName(rs.getString("MPA_NAME"));
                film.setMpa(mpa);

                Genre genre = new Genre();
                genre.setId(rs.getInt("GENRE_ID"));
                genre.setName(rs.getString("GENRE_NAME"));

                if (!data.containsKey(film)) {
                    data.put(film, new ArrayList<>());
                }
                data.get(film).add(genre);
                sortFilms.put(sortCount, data);
                ++sortCount;
            }
            return sortFilms;
        });
        Map<Long, Set<Director>> directorsAndFilms = jdbcTemplate.query(sqlDirectors.toString(), new DirectorExtractor());
        List<Film> films = new ArrayList<>();
        for (Map<Film, List<Genre>> filmsWithGenre : sortFilmsWithGenre.values()) {
            for (Film film : filmsWithGenre.keySet()) {
                Set<Genre> genres = new HashSet<>();
                for (Genre genre : filmsWithGenre.get(film)) {
                    if (genre.getId() != 0) {
                        genres.add(genre);
                    }
                }
                film.setGenres(genres);
                Set<Director> directors = new HashSet<>();
                if (directorsAndFilms.containsKey(film.getId())) {
                    directors = directorsAndFilms.get(film.getId());
                }
                film.setDirectors(directors);
                films.add(film);
            }
        }
        return films;
    }
}
