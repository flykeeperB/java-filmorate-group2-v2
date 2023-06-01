package ru.yandex.practicum.filmorate.storage.database.genres;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class GenreMapper implements RowMapper<Genre> {
    @Override
    public Genre mapRow(ResultSet rs, int rowNum) throws SQLException {
        Genre genre = new Genre();

        genre.setId(rs.getLong("GENRE_ID"));
        genre.setName(rs.getString("GENRE_NAME"));

        return genre;
    }
}
