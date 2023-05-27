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
import ru.yandex.practicum.filmorate.storage.database.directors.DirectorExtractor;

import java.util.*;

@Component("extraFunctionalFilmDbStorage")
@Repository
public class ExtraFunctionalFilmDbStorage extends FilmDbStorage implements ExtraFunctionalFilmStorage {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ExtraFunctionalFilmDbStorage(JdbcTemplate jdbcTemplate,
                                        @Qualifier("userDbStorage") UserStorage userStorage) {
        super(jdbcTemplate, userStorage);
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
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
                sqlFilms.append("upper(FILM_NAME) LIKE '%").append(query.toUpperCase()).append("%' ");
                sqlDirectors.append("d.FILM_ID IN (SELECT FILM_ID FROM FIlMS " +
                        "WHERE upper(FILM_NAME) LIKE '%").append(query.toUpperCase()).append("%')");
            } else if (byArr[i].equals("director")) {
                sqlFilms.append("f.FILM_ID IN " + "(SELECT FILM_ID FROM DIRECTORS WHERE DIRECTOR_ID IN " +
                        "(SELECT DIRECTOR_ID FROM LIST_OF_DIRECTORS WHERE upper(DIRECTOR_NAME) " +
                        "LIKE '%").append(query.toUpperCase()).append("%')) ");
                sqlDirectors.append("upper(DIRECTOR_NAME) LIKE '%").append(query.toUpperCase()).append("%' ");
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
        Map<Long, Set<Director>> directorsAndFilms = jdbcTemplate.query(sqlDirectors.toString(),
                new DirectorExtractor());
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

    private List<Film> prepareFilmList(Map<Film, List<Genre>> filmsWithGenre) {
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
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        String sql = GET_FILMS_FROM_TABLE_SQL +
                "LEFT JOIN LIKES AS l ON l.FILM_ID = f.FILM_ID " +
                "WHERE USER_ID = ? OR USER_ID = ? GROUP BY f.FILM_ID HAVING COUNT(*) > 1";
        Map<Film, List<Genre>> filmsWithGenre = jdbcTemplate.query(sql, new FilmExtractor(), userId, friendId);

        return prepareFilmList(filmsWithGenre);
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Long genreId, Integer year) {

        var params = new MapSqlParameterSource()
                .addValue("genreId", genreId)
                .addValue("year", year)
                .addValue("limit", count);

        List<String> conditions = new ArrayList<>();

        if (genreId > 0) {
            conditions.add("f.FILM_ID IN (SELECT FILM_ID FROM GENRES WHERE GENRE_ID = :genreId)");
        }
        if (year > 0) {
            conditions.add("EXTRACT(YEAR FROM f.RELEASE_DATE) = :year");
        }

        String condition = "";
        if (!conditions.isEmpty()) {
            condition = "WHERE " + String.join(" AND ", conditions) + " ";
        }

        String limitCondition = "";
        if (count > 0) {
            limitCondition = " LIMIT :limit";
        }

        String query = GET_FILMS_FROM_TABLE_SQL +
                "LEFT JOIN (SELECT FILM_ID, COUNT(USER_ID) AS COUNT_LIKES FROM LIKES GROUP BY FILM_ID) AS likes " +
                "ON f.FILM_ID=likes.FILM_ID " +
                condition +
                "ORDER BY likes.COUNT_LIKES DESC" +
                limitCondition;

        Map<Film, List<Genre>> filmsWithGenre =
                namedParameterJdbcTemplate.query(query, params, new FilmExtractor());

        return prepareFilmList(filmsWithGenre);
    }
}
