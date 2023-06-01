package ru.yandex.practicum.filmorate.storage.database.films;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.DbConnector;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component("filmDbStorage")
@Repository
public class FilmDbStorage implements FilmStorage {

    protected static final String SQL_TABLE = "FILMS";
    protected static final String SQL_TABLE_ALIAS = "f";
    protected static final String SQL_TABLE_AND_ALIAS = SQL_TABLE + " as " + SQL_TABLE_ALIAS;
    protected static final String SQL_KEY_FIELD = "FILM_ID";

    protected static final String SQL_FIELD_FILM_NAME = "FILM_NAME";
    protected static final String SQL_FIELD_DESCRIPTION = "DESCRIPTION";
    protected static final String SQL_FIELD_RELEASE_DATE = "RELEASE_DATE";
    protected static final String SQL_FIELD_DURATION = "DURATION";

    protected static final String SQL_TABLE_MPA = "MPAS";
    protected static final String SQL_TABLE_MPA_ALIAS = "m";
    protected static final String SQL_FIELD_MPA_ID = "MPA_ID";
    protected static final String SQL_FIELD_MPA_NAME = "MPA_NAME";

    protected static final String SQL_TABLE_LIKES = "LIKES";
    protected static final String SQL_TABLE_LIKES_ALIAS = "l";

    protected static final String SQL_ORDER_BY = "COUNT(" + SQL_TABLE_LIKES_ALIAS + ".USER_ID) DESC";
    protected static final String SQL_GROUP = "GROUP BY " + SQL_TABLE_ALIAS + "." + SQL_KEY_FIELD;
    protected static final String SQL_LIMIT = "LIMIT :limit";

    protected static final String SQL_SELECT_SOURCE = "SELECT " +
            SQL_TABLE_ALIAS + "." + SQL_KEY_FIELD + ", " +
            SQL_TABLE_ALIAS + "." + SQL_FIELD_FILM_NAME + ", " +
            SQL_TABLE_ALIAS + "." + SQL_FIELD_DESCRIPTION + ", " +
            SQL_TABLE_ALIAS + "." + SQL_FIELD_RELEASE_DATE + ", " +
            SQL_TABLE_ALIAS + "." + SQL_FIELD_DURATION + ", " +
            SQL_TABLE_MPA_ALIAS + "." + SQL_FIELD_MPA_ID + ", " +
            SQL_TABLE_MPA_ALIAS + "." + SQL_FIELD_MPA_NAME + " " +
            "FROM " + SQL_TABLE_AND_ALIAS + " " +
            "LEFT JOIN " + SQL_TABLE_MPA + " as " + SQL_TABLE_MPA_ALIAS + " " +
            "ON " + SQL_TABLE_ALIAS + "." + SQL_FIELD_MPA_ID + "=" + SQL_TABLE_MPA_ALIAS + "." + SQL_FIELD_MPA_ID + " " +
            "LEFT JOIN " + SQL_TABLE_LIKES + " as " + SQL_TABLE_LIKES_ALIAS + " " +
            "ON " + SQL_TABLE_ALIAS + "." + SQL_KEY_FIELD + "=" + SQL_TABLE_LIKES_ALIAS + "." + SQL_KEY_FIELD + " ";

    protected static final String SQL_SELECT = SQL_SELECT_SOURCE + " " +
            SQL_GROUP + " ORDER BY COUNT(l.USER_ID) DESC";

    protected static final String SQL_SELECT_FROM_IDS = SQL_SELECT_SOURCE +
            " WHERE " + SQL_TABLE_ALIAS + "." + SQL_KEY_FIELD + " IN ( :ids ) " +
            SQL_GROUP + " ORDER BY COUNT(l.USER_ID) DESC";

    protected static final String SQL_UPDATE = "UPDATE " + SQL_TABLE_AND_ALIAS + " SET " +
            SQL_TABLE_ALIAS + "." + SQL_FIELD_FILM_NAME + "=:" + SQL_FIELD_FILM_NAME + ", " +
            SQL_TABLE_ALIAS + "." + SQL_FIELD_DESCRIPTION + "=:" + SQL_FIELD_DESCRIPTION + ", " +
            SQL_TABLE_ALIAS + "." + SQL_FIELD_RELEASE_DATE + "=:" + SQL_FIELD_RELEASE_DATE + ", " +
            SQL_TABLE_ALIAS + "." + SQL_FIELD_DURATION + "=:" + SQL_FIELD_DURATION + ", " +
            SQL_TABLE_ALIAS + "." + SQL_FIELD_MPA_ID + "=:" + SQL_FIELD_MPA_ID + " " +
            "WHERE " + SQL_TABLE_ALIAS + "." + SQL_KEY_FIELD + "=:" + SQL_KEY_FIELD;

    protected static final String SQL_DELETE = "DELETE FROM " +
            SQL_TABLE_AND_ALIAS +
            " WHERE " + SQL_TABLE_ALIAS + "." + SQL_KEY_FIELD + "=:" + SQL_KEY_FIELD;

    protected static final String SQL_SELECT_GENRES_FOR_FILMS = "SELECT fg.FILM_ID, fg.GENRE_ID, g.GENRE_NAME " +
            "FROM FILMS_GENRES as fg LEFT JOIN GENRES as g " +
            "ON fg.GENRE_ID=g.GENRE_ID " +
            "WHERE fg.FILM_ID IN ( :ids )";

    protected static final String SQL_SELECT_DIRECTORS_FOR_FILMS = "SELECT fd.FILM_ID, fd.DIRECTOR_ID, d.DIRECTOR_NAME " +
            "FROM FILMS_DIRECTORS as fd LEFT JOIN DIRECTORS as d " +
            "ON fd.DIRECTOR_ID = d.DIRECTOR_ID " +
            "WHERE fd.FILM_ID IN ( :ids )";

    protected static final String SQL_SELECT_LIKES_FOR_FILMS = "SELECT l.FILM_ID, l.USER_ID " +
            "FROM LIKES as l " +
            "WHERE l.FILM_ID IN ( :ids )";

    protected static final String SQL_INSERT_GENRE =
            "INSERT INTO FILMS_GENRES (FILM_ID, GENRE_ID) VALUES (:FILM_ID, :GENRE_ID)";
    protected static final String SQL_DELETE_GENRE =
            "DELETE FROM FILMS_GENRES WHERE FILM_ID=:FILM_ID";

    protected static final String SQL_INSERT_DIRECTOR =
            "INSERT INTO FILMS_DIRECTORS (DIRECTOR_ID, FILM_ID) VALUES (:DIRECTOR_ID, :FILM_ID)";
    protected static final String SQL_DELETE_DIRECTOR =
            "DELETE FROM FILMS_DIRECTORS WHERE FILM_ID=:FILM_ID";

    protected static final String SQL_INSERT_LIKE =
            "INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (:FILM_ID, :USER_ID)";
    protected static final String SQL_DELETE_LIKE =
            "DELETE FROM LIKES WHERE FILM_ID=:FILM_ID AND USER_ID=:USER_ID";


    protected final DbConnector<Film> dbConnector;

    protected final UserStorage userStorage;

    @Autowired
    public FilmDbStorage(@Qualifier("userDbStorage") UserStorage userStorage,
                         DbConnector<Film> dbConnector
    ) {
        this.dbConnector = dbConnector;
        this.userStorage = userStorage;
    }

    private void addGenres(Long genreId, Long filmId) {

        findFilmById(filmId);

        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("FILM_ID", filmId);
        params.addValue("GENRE_ID", genreId);

        dbConnector.runWithParams(SQL_INSERT_GENRE, params);
    }

    private void deleteGenre(Long filmId) {
        findFilmById(filmId);

        MapSqlParameterSource params = new MapSqlParameterSource(SQL_KEY_FIELD, filmId);
        dbConnector.runWithParams(SQL_DELETE_GENRE, params);
    }

    private void addDirectors(Long directorId, Long filmId) {
        findFilmById(filmId);

        MapSqlParameterSource params = new MapSqlParameterSource();

        params.addValue("FILM_ID", filmId);
        params.addValue("DIRECTOR_ID", directorId);

        dbConnector.runWithParams(SQL_INSERT_DIRECTOR, params);
    }

    private void deleteDirectors(Long filmId) {
        findFilmById(filmId);

        MapSqlParameterSource params = new MapSqlParameterSource(SQL_KEY_FIELD, filmId);
        dbConnector.runWithParams(SQL_DELETE_DIRECTOR, params);
    }

    @Override
    public Film addFilm(Film film) {

        final Map<String, Object> params = new HashMap<>();

        params.put(SQL_FIELD_FILM_NAME, film.getName());
        params.put(SQL_FIELD_DESCRIPTION, film.getDescription());
        params.put(SQL_FIELD_RELEASE_DATE, film.getReleaseDate());
        params.put(SQL_FIELD_DURATION, film.getDuration());
        params.put(SQL_FIELD_MPA_ID, film.getMpa().getId());

        Long id = dbConnector.create(SQL_TABLE,
                SQL_KEY_FIELD,
                params);

        Set<Genre> genres = film.getGenres();
        if (!genres.equals(new HashSet<>())) {
            for (Genre genre : genres) {
                addGenres(genre.getId(), id);
            }
        }

        Set<Director> directors = film.getDirectors();
        if (!directors.equals(new HashSet<>())) {
            for (Director director : directors) {
                addDirectors(director.getId(), id);
            }
        }

        return findFilmById(id);
    }

    @Override
    public Film updateFilm(Long filmId, Film film) {
        findFilmById(film.getId());

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(SQL_FIELD_FILM_NAME, film.getName());
        params.addValue(SQL_FIELD_DESCRIPTION, film.getDescription());
        params.addValue(SQL_FIELD_RELEASE_DATE, film.getReleaseDate());
        params.addValue(SQL_FIELD_DURATION, film.getDuration());
        params.addValue(SQL_FIELD_MPA_ID, film.getMpa().getId());
        params.addValue(SQL_KEY_FIELD, film.getId());

        if (dbConnector.runWithParams(SQL_UPDATE, params) != 0) {
            log.info("Запись успешно обновлена ");
        }

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

        return findFilmById(film.getId());
    }

    @Override
    public List<Film> findAllFilms() {
        return getFilms(null);
    }

    protected List<Film> getFilms(List<Long> ids) {
        return getFilms(ids, null);
    }

    protected List<Film> getFilms(List<Long> ids, String customQuery) {
        String query = customQuery;
        MapSqlParameterSource params = new MapSqlParameterSource();

        if (query == null) {
            query = SQL_SELECT;

            if (ids != null) {
                if (ids.isEmpty()) {
                    return new ArrayList<>();
                }

                query = SQL_SELECT_FROM_IDS;
            }
        }

        params.addValue("ids", ids);

        List<Film> result = dbConnector.queryWithParams(query, params, new FilmMapper());

        if (!result.isEmpty()) {
            List<Long> filmsIds = result
                    .stream()
                    .map(Film::getId)
                    .collect(Collectors.toList());

            Map<Long, List<Genre>> genres = getGenresForFilms(filmsIds);
            Map<Long, List<Director>> directors = getDirectorsForFilms(filmsIds);
            Map<Long, List<Long>> likes = getLikesForFilms(filmsIds);
            for (Film film : result) {
                film.setGenres(genres.getOrDefault(film.getId(), new ArrayList<>()));
                film.setDirectors(directors.getOrDefault(film.getId(), new ArrayList<>()));
                film.setLikes(likes.getOrDefault(film.getId(), new ArrayList<>()));
            }
        }

        return result;
    }

    @Override
    public Film findFilmById(Long filmId) {
        List<Film> films = getFilms(List.of(filmId));
        if (films.isEmpty()) {
            throw new NotFoundException("Фильма c таким id нет");
        }
        return films.get(0);
    }

    @Override
    public List<Film> getPopularFilms(Long limit) {

        List<Long> ids = null;

        if (limit != null) {

            MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);

            ids = dbConnector.queryWithParamsByCustomType(
                    SQL_SELECT + " " + SQL_LIMIT,
                    params, (rs, numRow) -> rs.getLong("f.FILM_ID")
            );
        }

        return getFilms(ids, SQL_ORDER_BY);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        userStorage.findUserById(userId);
        findFilmById(filmId);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(SQL_KEY_FIELD, filmId);
        params.addValue("USER_ID", userId);
        dbConnector.runWithParams(SQL_DELETE_LIKE, params);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        userStorage.findUserById(userId);
        findFilmById(filmId);

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue(SQL_KEY_FIELD, filmId);
        params.addValue("USER_ID", userId);
        try {
            dbConnector.runWithParams(SQL_INSERT_LIKE, params);
        } catch (RuntimeException e) {
            log.info("Ошибка при добавлении лайка");
        }
    }

    private Map<Long, List<Genre>> getGenresForFilms(List<Long> ids) {

        var params = new MapSqlParameterSource("ids", ids);

        Map<Long, List<Genre>> result = dbConnector.queryWithParamsByCustomTypeExtractor(SQL_SELECT_GENRES_FOR_FILMS,
                params, (rs) -> {
                    Map<Long, List<Genre>> subResult = new HashMap<>();
                    while (rs.next()) {
                        Genre genre = new Genre();
                        genre.setId(rs.getLong("GENRE_ID"));
                        genre.setName(rs.getString("GENRE_NAME"));
                        List<Genre> genresOfFilm = subResult
                                .getOrDefault(rs.getLong("FILM_ID"), new ArrayList<>());
                        genresOfFilm.add(genre);
                        subResult.put(rs.getLong("FILM_ID"), genresOfFilm);
                    }
                    return subResult;
                });

        return result;
    }

    private Map<Long, List<Director>> getDirectorsForFilms(List<Long> ids) {

        var params = new MapSqlParameterSource("ids", ids);

        Map<Long, List<Director>> result =
                dbConnector.queryWithParamsByCustomTypeExtractor(SQL_SELECT_DIRECTORS_FOR_FILMS,
                        params, (rs) -> {
                            Map<Long, List<Director>> subResult = new HashMap<>();
                            while (rs.next()) {
                                Director director = new Director();
                                director.setId(rs.getLong("DIRECTOR_ID"));
                                director.setName(rs.getString("DIRECTOR_NAME"));
                                List<Director> directorsOfFilm = subResult
                                        .getOrDefault(rs.getLong("FILM_ID"), new ArrayList<>());
                                directorsOfFilm.add(director);
                                subResult.put(rs.getLong("FILM_ID"), directorsOfFilm);
                            }
                            return subResult;
                        });

        return result;
    }

    private Map<Long, List<Long>> getLikesForFilms(List<Long> ids) {
        var params = new MapSqlParameterSource("ids", ids);

        Map<Long, List<Long>> result =
                dbConnector.queryWithParamsByCustomTypeExtractor(SQL_SELECT_LIKES_FOR_FILMS,
                        params, (rs) -> {
                            Map<Long, List<Long>> subResult = new HashMap<>();
                            while (rs.next()) {
                                List<Long> likesOfFilm = subResult
                                        .getOrDefault(rs.getLong("FILM_ID"), new ArrayList<>());
                                likesOfFilm.add(rs.getLong("USER_ID"));
                                subResult.put(rs.getLong("FILM_ID"), likesOfFilm);
                            }
                            return subResult;
                        });

        return result;
    }

    @Override
    public void deleteFilmById(Long filmId) {

        findFilmById(filmId);

        MapSqlParameterSource params = new MapSqlParameterSource(SQL_KEY_FIELD, filmId);

        if (dbConnector.runWithParams(SQL_DELETE, params) != 0) {
            log.info("Запись успешно удалена ");
        }
    }
}
