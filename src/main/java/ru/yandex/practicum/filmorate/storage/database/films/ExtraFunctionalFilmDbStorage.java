package ru.yandex.practicum.filmorate.storage.database.films;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.ExtraFunctionalFilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.MainSqlQueryConstructor;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component("extraFunctionalFilmDbStorage")
@Repository
public class ExtraFunctionalFilmDbStorage extends FilmDbStorage implements ExtraFunctionalFilmStorage {

    @Autowired
    public ExtraFunctionalFilmDbStorage(JdbcTemplate jdbcTemplate,
                                        @Qualifier("userDbStorage") UserStorage userStorage) {
        super(jdbcTemplate, userStorage);
    }

    @Override
    public List<Film> searchFilms(String searchText, String by) {
        String[] byArr = by.split(",");

        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("f.FILM_ID")
                .fromPart("FILMS as f " +
                        "LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS COUNT_LIKES " +
                        "FROM LIKES GROUP BY FILM_ID) AS likes " +
                        "ON f.FILM_ID=likes.FILM_ID ")
                .build();

        List<String> conditions = new ArrayList<>();

        var params = new MapSqlParameterSource();

        for (String s : byArr) {
            if (s.equals("title")) {
                conditions.add("FILM_NAME ~*:film1search ");
                params.addValue("film1search", searchText);

            } else if (s.equals("director")) {
                conditions.add("f.FILM_ID IN " + "(SELECT FILM_ID FROM DIRECTORS WHERE DIRECTOR_ID IN " +
                        "(SELECT DIRECTOR_ID FROM LIST_OF_DIRECTORS WHERE DIRECTOR_NAME " +
                        "~*:film2search)) ");
                params.addValue("film2search", searchText);
            }

            if (!conditions.isEmpty()) {
                queryConstructor.setWherePart(String.join(" AND ", conditions));
            }
        }

        String query = queryConstructor.getSelectQuery();

        List<Long> ids = namedParameterJdbcTemplate.query(query,
                params,
                (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids, "COUNT(l.USER_ID)");
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("f.FILM_ID")
                .fromPart("FILMS as f LEFT JOIN LIKES as l ON f.FILM_ID=l.FILM_ID")
                .wherePart("USER_ID = :userId OR USER_ID = :friendId GROUP BY f.FILM_ID HAVING COUNT(*) > 1")
                .build();

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        List<Long> ids = namedParameterJdbcTemplate.query(query,
                params,
                (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids);
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Long genreId, Integer year) {
        log.info("getPopularFilms");
        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("f.FILM_ID")
                .fromPart("FILMS as f LEFT JOIN LIKES as l ON f.FILM_ID=l.FILM_ID")
                .groupPart("f.FILM_ID")
                .orderPart("COUNT(l.USER_ID) DESC")
                .build();

        if (count != null) {
            queryConstructor.setLimitPart(":limit");
        }

        List<String> conditions = new ArrayList<>();

        if (genreId > 0) {
            conditions.add("f.FILM_ID IN (SELECT gr.FILM_ID FROM GENRES as gr WHERE gr.GENRE_ID = :genreId)");
        }

        if (year > 0) {
            conditions.add("EXTRACT(YEAR FROM f.RELEASE_DATE) = :year");
        }

        if (!conditions.isEmpty()) {
            queryConstructor.setWherePart(String.join(" AND ", conditions));
        }

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource()
                .addValue("limit", count)
                .addValue("genreId", genreId)
                .addValue("year", year);

        List<Long> ids = new ArrayList<>();

        try {
            ids = namedParameterJdbcTemplate.query(query,
                    params,
                    (rs, numRow) -> rs.getLong("FILM_ID"));
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }

        return getFilms(ids, "COUNT(l.USER_ID)");
    }

    @Override
    public List<Film> getFilmsByDirectorSortByYear(Long directorId) {

        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("f.FILM_ID")
                .fromPart("FILMS as f LEFT JOIN DIRECTORS as d " +
                        "ON d.FILM_ID = f.FILM_ID ")
                .groupPart("FILM_ID")
                .orderPart("EXTRACT(YEAR FROM f.RELEASE_DATE) DESC")
                .wherePart("d.DIRECTOR_ID=:directorId")
                .build();

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource()
                .addValue("directorId", directorId);

        List<Long> ids = namedParameterJdbcTemplate.query(query,
                params,
                (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids, "EXTRACT(YEAR FROM f.RELEASE_DATE)");
    }

    @Override
    public List<Film> getFilmsByDirectorSortByLikes(Long directorId) {

        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("f.FILM_ID")
                .fromPart("FILMS as f " +
                        "LEFT JOIN DIRECTORS as d " +
                        "ON d.FILM_ID = f.FILM_ID " +
                        "LEFT JOIN LIKES as lk " +
                        "ON lk.FILM_ID = f.FILM_ID")
                .groupPart("FILM_ID")
                .orderPart("COUNT(lk.USER_ID) DESC")
                .wherePart("d.DIRECTOR_ID=:directorId")
                .build();

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource()
                .addValue("directorId", directorId);

        List<Long> ids = namedParameterJdbcTemplate.query(query,
                params,
                (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids, "COUNT(l.USER_ID) DESC");
    }
}