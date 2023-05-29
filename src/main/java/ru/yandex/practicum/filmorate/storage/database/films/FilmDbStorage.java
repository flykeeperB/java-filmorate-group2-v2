package ru.yandex.practicum.filmorate.storage.database.films;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.MainSqlQueryConstructor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.stream.Collectors;


@Slf4j
@Component("filmDbStorage")
@Repository
public class FilmDbStorage implements FilmStorage {

    protected static final String FIELDS_PART_OF_SELECT_QUERY =
            "f.FILM_ID, " +
                    "f.FILM_NAME, " +
                    "f.DESCRIPTION," +
                    "f.RELEASE_DATE, " +
                    "f.DURATION, " +
                    "f.MPA_ID, " +
                    "m.MPA_NAME";

    protected static final String FROM_PART_OF_SELECT_QUERY =
            "FILMS AS f " +
                    "LEFT JOIN LIST_OF_MPAS AS m ON f.MPA_ID = m.MPA_ID " +
                    "LEFT JOIN LIKES as l ON l.FILM_ID = f.FILM_ID";

    protected static final String GROUP_PART_OF_SELECT_QUERY =
            "f.FILM_ID";

    protected final JdbcTemplate jdbcTemplate;

    protected final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected final UserStorage userStorage;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         @Qualifier("userDbStorage") UserStorage userStorage) {
        this.jdbcTemplate = jdbcTemplate;
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
        this.userStorage = userStorage;
    }

    private void addGenres(long idGenre, long idFilm) {
        try {
            String sqlQuery = "INSERT INTO GENRES (FILM_ID, GENRE_ID) VALUES (?,?)";

            jdbcTemplate.update(sqlQuery, idFilm, idGenre);
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
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
            log.error(e.getMessage());
        }
    }

    private void deleteDirectors(long idFilm) {
        String sqlQuery = "DELETE FROM DIRECTORS WHERE FILM_ID=?";

        jdbcTemplate.update(sqlQuery, idFilm);
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

        findFilmById(film.getId());


        String sqlQuery = "UPDATE FILMS SET FILM_NAME=?, DESCRIPTION=?, RELEASE_DATE=?, DURATION=?, MPA_ID=? "
                + "WHERE FILM_ID=?";

        try {
            jdbcTemplate.update(
                    sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    filmId);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
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
        return film;
    }

    @Override
    public List<Film> findAllFilms() {
        return getFilms(null);
    }

    @Override
    public List<Film> getFilms(List<Long> ids) {
        log.info("Storage.getFilms()");
        return getFilms(ids, null);
    }

    @Override
    public List<Film> getFilms(List<Long> ids, String orderFields) {

        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart(FIELDS_PART_OF_SELECT_QUERY)
                .fromPart(FROM_PART_OF_SELECT_QUERY)
                .groupPart(GROUP_PART_OF_SELECT_QUERY)
                .build();

        if (ids != null) {
            if (ids.isEmpty()) {
                log.info("getFilms return = new ArrayList<>()");
                return new ArrayList<>();
            } else {
                queryConstructor.setWherePart("f.FILM_ID IN (:ids)");
            }
        }

        if (orderFields != null) {
            if (!orderFields.isEmpty()) {
                queryConstructor.setOrderPart(orderFields);
            }
        }

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource();
        if (ids != null) {
            params.addValue("ids", ids);
        }

        List<Film> result = new ArrayList<>();

        try {
            result = namedParameterJdbcTemplate.query(query, params, new FilmMapper());
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }

        if (!result.isEmpty()) {
            Map<Long, List<Genre>> genres = getGenresForFilms(result);
            Map<Long, List<Director>> directors = getDirectorsForFilms(result);
            Map<Long, List<Long>> likes = getLikesForFilms(result);

            for (Film film : result) {
                film.setGenres(genres.getOrDefault(film.getId(), new ArrayList<>()));
                film.setDirectors(directors.getOrDefault(film.getId(), new ArrayList<>()));
                film.setLikes(likes.getOrDefault(film.getId(), new ArrayList<>()));
            }
        }

        return result;
    }

    @Override
    public Film findFilmById(long filmId) {
        List<Film> films = getFilms(List.of(filmId));

        if (films.isEmpty()) {
            throw new NotFoundException("Фильма c таким id нет");
        }

        return films.get(0);
    }

    @Override
    public List<Film> getPopularFilms(int count) {

        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("FILM_ID")
                .fromPart("LIKES")
                .groupPart("FILM_ID")
                .orderPart("ORDER BY COUNT(USER_ID)")
                .limitPart(":limit")
                .build();

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource().addValue("limit", count);

        List<Long> ids = namedParameterJdbcTemplate.query(query,
                params,
                (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        userStorage.findUserById(userId);
        findFilmById(filmId);

        String sqlQuery = "DELETE FROM LIKES WHERE FILM_ID=? AND USER_ID=?";
        jdbcTemplate.update(sqlQuery, filmId, userId);
    }

    @Override
    public void addLike(long filmId, long userId) {
        userStorage.findUserById(userId);
        findFilmById(filmId);

        String sql = "INSERT INTO LIKES (FILM_ID, USER_ID) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    private Map<Long, List<Genre>> getGenresForFilms(List<Film> films) {

        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("g.FILM_ID, g.GENRE_ID, lg.GENRE_NAME")
                .fromPart("GENRES as g LEFT JOIN LIST_OF_GENRES as lg " +
                        "ON g.GENRE_ID = lg.GENRE_ID")
                .wherePart("g.FILM_ID IN ( :ids )")
                .build();

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource();
        if (films != null) {
            params.addValue("ids", films.stream()
                    .map(Film::getId)
                    .collect(Collectors.toList()));
        }

        Map<Long, List<Genre>> result = new HashMap<>();
        try {
            result = namedParameterJdbcTemplate.query(query, params, (rs) -> {
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

        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }

        if (result == null) {
            return new HashMap<>();
        }

        return result;
    }

    private Map<Long, List<Director>> getDirectorsForFilms(List<Film> films) {
        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("d.FILM_ID, ld.DIRECTOR_ID, ld.DIRECTOR_NAME")
                .fromPart("DIRECTORS as d LEFT JOIN LIST_OF_DIRECTORS as ld " +
                        "ON d.DIRECTOR_ID = ld.DIRECTOR_ID")
                .wherePart("d.FILM_ID IN ( :ids )")
                .build();

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource();
        if (films != null) {
            params.addValue("ids", films.stream()
                    .map(Film::getId)
                    .collect(Collectors.toList()));
        }

        Map<Long, List<Director>> result = new HashMap<>();
        try {
            result = namedParameterJdbcTemplate.query(query, params, (rs) -> {
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
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }

        return result;
    }

    private Map<Long, List<Long>> getLikesForFilms(List<Film> films) {
        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("FILM_ID, USER_ID")
                .fromPart("LIKES")
                .wherePart("FILM_ID IN (:ids)")
                .build();

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource();
        if (films != null) {
            params.addValue("ids", films.stream()
                    .map(Film::getId)
                    .collect(Collectors.toList()));
        }

        Map<Long, List<Long>> result = new HashMap<>();
        try {
            result = namedParameterJdbcTemplate.query(query, params, (rs) -> {
                Map<Long, List<Long>> subResult = new HashMap<>();
                while (rs.next()) {
                    List<Long> likesOfFilm =
                            subResult.getOrDefault(rs.getLong("FILM_ID"), new ArrayList<>());

                    likesOfFilm.add(rs.getLong("USER_ID"));

                    subResult.put(rs.getLong("FILM_ID"), likesOfFilm);
                }
                return subResult;
            });
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }

        return result;
    }

}
