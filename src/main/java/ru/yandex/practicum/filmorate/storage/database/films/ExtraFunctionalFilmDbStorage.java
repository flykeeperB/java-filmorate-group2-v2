package ru.yandex.practicum.filmorate.storage.database.films;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.ExtraFunctionalFilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("extraFunctionalFilmDbStorage")
@Repository
public class ExtraFunctionalFilmDbStorage extends FilmDbStorage implements ExtraFunctionalFilmStorage {

    @Autowired
    public ExtraFunctionalFilmDbStorage(JdbcTemplate jdbcTemplate,
                                        @Qualifier("userDbStorage") UserStorage userStorage) {
        super(jdbcTemplate, userStorage);
    }

    NamedParameterJdbcTemplate parameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    static final String GET_FILM_ID_FROM_TABLE_SQL = "SELECT f.FILM_ID FROM FILMS AS f ";

    @Override
    public List<Film> searchFilms(String query, String by) {
        String[] byArr = by.split(",");
        Map<String, Object> filmsParam = new HashMap<>();
        StringBuilder sqlFilms = new StringBuilder(GET_FILM_ID_FROM_TABLE_SQL +
                "LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS COUNT_LIKES FROM LIKES GROUP BY FILM_ID) AS likes " +
                "ON f.FILM_ID=likes.FILM_ID WHERE ");
        for (int i = 0; i < byArr.length; i++) {
            if (byArr[i].equals("title")) {
                sqlFilms.append("upper(FILM_NAME) ~*:film1search ");
                filmsParam.put("film1search", query.toUpperCase());

            } else if (byArr[i].equals("director")) {
                sqlFilms.append("f.FILM_ID IN " + "(SELECT FILM_ID FROM DIRECTORS WHERE DIRECTOR_ID IN " +
                        "(SELECT DIRECTOR_ID FROM LIST_OF_DIRECTORS WHERE upper(DIRECTOR_NAME) " +
                        "~*:film2search)) ");
                filmsParam.put("film2search", query.toUpperCase());
            }
            if (i != byArr.length - 1) {
                sqlFilms.append(" OR ");
            }
        }
        sqlFilms.append("ORDER BY likes.COUNT_LIKES DESC ");

        return getFilms(parameterJdbcTemplate.query(sqlFilms.toString()
                , filmsParam, (rs) -> {
                    List<Long> idFilm = new ArrayList<>();
                    while (rs.next()) {
                        idFilm.add(rs.getLong("FILM_ID"));
                    }
                    return idFilm;
                }));
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        Map<String, Object> param = new HashMap<>();
        String sql = GET_FILM_ID_FROM_TABLE_SQL +
                "LEFT JOIN LIKES AS l ON l.FILM_ID = f.FILM_ID " +
                "WHERE USER_ID = :userId OR USER_ID = :friendId GROUP BY f.FILM_ID HAVING COUNT(*) > 1";
        param.put("userId", userId);
        param.put("friendId", friendId);

        return getFilms(parameterJdbcTemplate.query(sql, param, (rs) -> {
            List<Long> idFilm = new ArrayList<>();
            while (rs.next()) {
                idFilm.add(rs.getLong("FILM_ID"));
            }
            return idFilm;
        }));
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Long genreId, Integer year) {

        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart("FILM_ID")
                .fromPart("FILMS as f LEFT JOIN LIKES as l ON f.FILM_ID=l.FILM_ID")
                .groupPart("FILM_ID")
                .orderPart("ORDER BY COUNT(USER_ID)")
                .build();

        if (count != null) {
            queryConstructor.setLimitPart(":limit");
        }

        List<String> conditions = new ArrayList<>();

        if (genreId > 0) {
            conditions.add("f.FILM_ID IN (SELECT FILM_ID FROM GENRES WHERE GENRE_ID = :genreId)");
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

        List<Long> ids = namedParameterJdbcTemplate.query(query,
                params,
                (rs, numRow)->rs.getLong("FILM_ID"));

        return getFilms(ids);
    }
}