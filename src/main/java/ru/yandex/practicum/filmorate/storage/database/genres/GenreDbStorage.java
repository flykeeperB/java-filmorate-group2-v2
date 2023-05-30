package ru.yandex.practicum.filmorate.storage.database.genres;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
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

    private static final String SQL_GET_ALL_GENRES = "SELECT * FROM LIST_OF_GENRES";
    private static final String SQL_GET_GENRE_ID = "SELECT GENRE_ID FROM LIST_OF_GENRES WHERE GENRE_ID=?";
    private static final String SQL_GET_ALL_GENRE = "SELECT * FROM LIST_OF_GENRES WHERE GENRE_ID=?";

    @Override
    public List<Genre> findAllGenres() {
        return jdbcTemplate.query(SQL_GET_ALL_GENRES, genreMapper);
    }

    @Override
    public Genre findGenreById(long genreId) {
        Genre genre = new Genre();
        try {
            boolean exists = false;
            int count = jdbcTemplate.queryForObject(SQL_GET_GENRE_ID, new Object[]{genreId}, Integer.class);
            exists = count > 0;
            if (exists) {
                genre = jdbcTemplate.query(SQL_GET_ALL_GENRE, new Object[]{genreId}, genreMapper)
                        .stream().findAny().orElse(null);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Genre с таким id нет");
        }
        return genre;

    }
}
