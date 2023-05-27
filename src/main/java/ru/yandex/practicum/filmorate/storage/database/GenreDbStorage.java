package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.GenreNotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private GenreMapper genreMapper;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> findAllGenres() {
        String sql = "SELECT * FROM LIST_OF_GENRES";
        return jdbcTemplate.query(sql, genreMapper);
    }

    @Override
    public Genre findGenreById(long genreId) {
        Genre genre = new Genre();
        try {
            String sql = "SELECT GENRE_ID FROM LIST_OF_GENRES WHERE GENRE_ID=?";
            boolean exists = false;
            int count = jdbcTemplate.queryForObject(sql, new Object[]{genreId}, Integer.class);
            exists = count > 0;
            if (exists) {
                genre = jdbcTemplate.query("SELECT * FROM LIST_OF_GENRES WHERE GENRE_ID=?", new Object[]{genreId}, genreMapper)
                        .stream().findAny().orElse(null);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new GenreNotFoundException("Genre с таким id нет");
        }
        return genre;

    }
}
