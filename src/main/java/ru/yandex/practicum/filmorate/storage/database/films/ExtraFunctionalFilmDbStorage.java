package ru.yandex.practicum.filmorate.storage.database.films;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.ExtraFunctionalFilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.DbConnector;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Repository("extraFunctionalFilmDbStorage")
public class ExtraFunctionalFilmDbStorage extends FilmDbStorage implements ExtraFunctionalFilmStorage {

    protected static final String SQL_SEARCH = "SELECT f.FILM_ID FROM FILMS as f " +
            "LEFT JOIN FILMS_DIRECTORS as d " +
            "ON f.FILM_ID = d.FILM_ID " +
            "LEFT JOIN DIRECTORS as dl " +
            "ON d.DIRECTOR_ID=dl.DIRECTOR_ID";

    protected static final String SQL_SEARCH_BY_TITLE = SQL_SEARCH +
            " WHERE f.FILM_NAME ~*:searchText";

    protected static final String SQL_SEARCH_BY_DIRECTOR = SQL_SEARCH +
            " WHERE dl.DIRECTOR_NAME ~*:searchText";

    protected static final String SQL_SEARCH_BY_TITLE_AND_DIRECTOR = SQL_SEARCH +
            " WHERE (f.FILM_NAME ~*:searchText) OR (dl.DIRECTOR_NAME ~*:searchText)";

    protected static final String SQL_COMMON = "SELECT f.FILM_ID FROM FILMS as f " +
            "LEFT JOIN LIKES as l ON f.FILM_ID=l.FILM_ID " +
            "WHERE " +
            "USER_ID = :userId OR USER_ID = :friendId GROUP BY f.FILM_ID HAVING COUNT(*) > 1";

    protected static final String SQL_POPULAR = SQL_SELECT_SOURCE +
            SQL_GROUP + " " +
            "ORDER BY COUNT(" + SQL_TABLE_LIKES_ALIAS + ".USER_ID) DESC " +
            SQL_LIMIT;

    protected static final String SQL_POPULAR_BY_GENRE = SQL_SELECT_SOURCE + " WHERE " +
            "f.FILM_ID IN " +
            "(SELECT gr.FILM_ID " +
            "FROM FILMS_GENRES as gr WHERE gr.GENRE_ID = :genreId) " +
            SQL_GROUP + " " +
            "ORDER BY COUNT(" + SQL_TABLE_LIKES_ALIAS + ".USER_ID) DESC " +
            SQL_LIMIT;

    protected static final String SQL_POPULAR_BY_YEAR = SQL_SELECT_SOURCE + " WHERE " +
            "EXTRACT(YEAR FROM f.RELEASE_DATE) = :year " +
            SQL_GROUP + " " +
            "ORDER BY COUNT(" + SQL_TABLE_LIKES_ALIAS + ".USER_ID) DESC " +
            SQL_LIMIT;

    protected static final String SQL_POPULAR_BY_GENRE_AND_YEAR = SQL_SELECT_SOURCE + " WHERE " +
            "EXTRACT(YEAR FROM f.RELEASE_DATE) = :year " +
            "AND f.FILM_ID IN (SELECT gr.FILM_ID FROM FILMS_GENRES as gr WHERE gr.GENRE_ID = :genreId) " +
            SQL_GROUP + " " +
            "ORDER BY COUNT(" + SQL_TABLE_LIKES_ALIAS + ".USER_ID) DESC " +
            SQL_LIMIT;

    protected static final String SQL_GET_BY_DIRECTOR_SORT_BY_YEAR =
            "SELECT f.FILM_ID FROM FILMS as f " +
                    "LEFT JOIN FILMS_DIRECTORS as d " +
                    "ON d.FILM_ID = f.FILM_ID " +
                    "WHERE d.DIRECTOR_ID=:directorId " +
                    "GROUP BY f.FILM_ID ";

    protected static final String SQL_GET_BY_DIRECTOR_SORT_BY_LIKES =
            "SELECT f.FILM_ID FROM FILMS as f " +
                    "LEFT JOIN FILMS_DIRECTORS as d " +
                    "ON d.FILM_ID = f.FILM_ID " +
                    "LEFT JOIN LIKES as lk " +
                    "ON lk.FILM_ID = f.FILM_ID " +
                    "WHERE d.DIRECTOR_ID=:directorId " +
                    "GROUP BY f.FILM_ID " +
                    "ORDER BY COUNT(lk.USER_ID) DESC ";

    protected static final String SQL_GET_RECOMMENDATIONS = "SELECT f.FILM_ID FROM " +
            "LIKES as l1 " +
            "JOIN LIKES as l2 " +
            "ON l2.FILM_ID = l1.FILM_ID " +
            "JOIN LIKES as l3 " +
            "ON l2.FILM_ID = l3.FILM_ID " +
            "LEFT JOIN LIKES as l4 " +
            "ON l4.FILM_ID = l3.FILM_ID AND l4.USER_ID = :userId " +
            "LEFT JOIN FILMS as f " +
            "ON l3.FILM_ID = f.FILM_ID " +
            "WHERE l4.FILM_ID IS NULL OR l4.USER_ID IS NULL " +
            "GROUP BY l3.FILM_ID, f.FILM_ID";

    protected static final String SQL_SELECT_FROM_IDS_ORDER_BY_YEAR =
            SQL_SELECT_SOURCE +
                    "WHERE f.FILM_ID IN ( :ids ) " +
                    "GROUP BY f.FILM_ID ORDER BY EXTRACT(YEAR FROM f.RELEASE_DATE)";

    protected final DirectorStorage directorStorage;

    @Autowired
    public ExtraFunctionalFilmDbStorage(DbConnector<Film> dbConnector,
                                        @Qualifier("userDbStorage") UserStorage userStorage,
                                        @Qualifier("directorDbStorage") DirectorStorage directorStorage) {

        super(userStorage, dbConnector);

        this.directorStorage = directorStorage;
    }

    @Override
    public List<Film> searchFilms(String searchText, String by) {
        String[] byArr = by.split(",");

        var params = new MapSqlParameterSource("searchText", searchText);

        String query = SQL_SEARCH;

        if (Arrays.asList(byArr).contains("title") &&
                Arrays.asList(byArr).contains("director")) {
            log.info("Поиск по названию и по директору");
            query = SQL_SEARCH_BY_TITLE_AND_DIRECTOR;
        } else {
            if (Arrays.asList(byArr).contains("title")) {
                query = SQL_SEARCH_BY_TITLE;
                log.info("Поиск по названию");
            }
            if (Arrays.asList(byArr).contains("director")) {
                query = SQL_SEARCH_BY_DIRECTOR;
                log.info("Поиск по директору");
            }
        }

        List<Long> ids = dbConnector
                .queryWithParamsByCustomType(query,
                        params,
                        (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {

        var params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        List<Long> ids = dbConnector
                .queryWithParamsByCustomType(SQL_COMMON,
                        params,
                        (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids);
    }

    @Override
    public List<Film> getPopularFilms(Integer limit, Long genreId, Integer year) {

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (limit != null) {
            params.addValue("limit", limit);
        }

        String query = SQL_POPULAR;
        if ((genreId > 0) && (year > 0)) {
            query = SQL_POPULAR_BY_GENRE_AND_YEAR;
            params.addValue("genreId", genreId);
            params.addValue("year", year);
        } else {
            if (genreId > 0) {
                params.addValue("genreId", genreId);
                query = SQL_POPULAR_BY_GENRE;
            }
            if (year > 0) {
                params.addValue("year", year);
                query = SQL_POPULAR_BY_YEAR;
            }
        }

        List<Long> ids = dbConnector
                .queryWithParamsByCustomType(query,
                        params,
                        (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids);
    }

    @Override
    public List<Film> getFilmsByDirectorSortByYear(Long directorId) {
        directorStorage.findDirectorById(directorId);

        var params = new MapSqlParameterSource()
                .addValue("directorId", directorId);

        List<Long> ids = dbConnector
                .queryWithParamsByCustomType(SQL_GET_BY_DIRECTOR_SORT_BY_YEAR,
                        params,
                        (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids, SQL_SELECT_FROM_IDS_ORDER_BY_YEAR);
    }

    @Override
    public List<Film> getFilmsByDirectorSortByLikes(Long directorId) {

        directorStorage.findDirectorById(directorId);

        var params = new MapSqlParameterSource()
                .addValue("directorId", directorId);

        List<Long> ids = dbConnector
                .queryWithParamsByCustomType(SQL_GET_BY_DIRECTOR_SORT_BY_LIKES,
                        params,
                        (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids);
    }

    @Override
    public List<Film> getFilmsRecommendations(Long userId) {

        userStorage.findUserById(userId);

        var params = new MapSqlParameterSource()
                .addValue("userId", userId);

        List<Long> ids = dbConnector
                .queryWithParamsByCustomType(SQL_GET_RECOMMENDATIONS,
                        params,
                        (rs, numRow) -> rs.getLong("FILM_ID"));

        return getFilms(ids);

    }
}