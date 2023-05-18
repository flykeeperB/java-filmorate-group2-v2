package ru.yandex.practicum.filmorate.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenreExtractor implements ResultSetExtractor<List<Genre>> {
    @Override
    public List<Genre> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<Genre> data = new ArrayList<>();
        while (rs.next()) {
            Genre genre = new Genre();
            genre.setId(rs.getInt("GENRE_ID"));
            genre.setName(rs.getString("GENRE_NAME"));

        }
        return data;
    }
}
