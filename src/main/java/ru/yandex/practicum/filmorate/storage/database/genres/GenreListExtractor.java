package ru.yandex.practicum.filmorate.storage.database.genres;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenreListExtractor implements ResultSetExtractor<Map<Long, Set<Genre>>> {
    @Override
    public Map<Long, Set<Genre>> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Set<Genre>> data = new HashMap<>();
        while (rs.next()) {
           Genre genre = new Genre();
            genre.setId(rs.getInt("GENRE_ID"));
            genre.setName(rs.getString("GENRE_NAME"));

            Long filmId = rs.getLong("FILM_ID");

            if (!data.containsKey(filmId)) {
                data.put(filmId, new HashSet<>());
            }

            data.get(filmId).add(genre);
        }
        return data;
    }
}

