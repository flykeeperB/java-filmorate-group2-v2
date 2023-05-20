package ru.yandex.practicum.filmorate.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.DirectorNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private DirectorMapper directorMapper;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> findAllDirectors() {
        String sql = "SELECT * FROM LIST_OF_DIRECTORS";
        return jdbcTemplate.query(sql, directorMapper);
    }

    @Override
    public Director findDirectorById(long directorId) {
        Director director = new Director();
        try {
            String sql = "SELECT DIRECTOR_ID FROM LIST_OF_DIRECTORS WHERE DIRECTOR_ID=?";
            boolean exists = false;
            int count = jdbcTemplate.queryForObject(sql, new Object[]{directorId}, Integer.class);
            exists = count > 0;

            if (exists) {
                director = jdbcTemplate.query("SELECT * FROM LIST_OF_DIRECTORS WHERE DIRECTOR_ID=?"
                        , new Object[]{directorId}, directorMapper).stream().findAny().orElse(null);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new DirectorNotFoundException("Режиссёра c таким id нет");
        }
        return director;
    }

    @Override
    public Director addDirector(Director director) {
        SimpleJdbcInsert insertIntoDirector = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("LIST_OF_DIRECTORS")
                .usingGeneratedKeyColumns("DIRECTOR_ID");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("DIRECTOR_NAME", director.getName());
        Number newId = insertIntoDirector.executeAndReturnKey(parameters);
        director.setId(newId.longValue());

        return director;
    }

    @Override
    public Director updateDirector(long id, Director director) {
        try {
            String sql = "SELECT DIRECTOR_ID FROM LIST_OF_DIRECTORS WHERE DIRECTOR_ID=?";
            boolean exists = false;
            int count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
            exists = count > 0;

            if (exists) {
                String sqlQuery = "UPDATE LIST_OF_DIRECTORS SET DIRECTOR_NAME=? WHERE DIRECTOR_ID=?";
                jdbcTemplate.update(sqlQuery, director.getName(), id);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new DirectorNotFoundException("Такого режиссёра нет");
        }

        return director;
    }

    @Override
    public void deleteDirector(long id) {
        String sqlDelete = "DELETE FROM DIRECTORS WHERE DIRECTOR_ID=?";
        String sqlQuery = "DELETE FROM LIST_OF_DIRECTORS WHERE DIRECTOR_ID=?";

        jdbcTemplate.update(sqlDelete, id);
        jdbcTemplate.update(sqlQuery, id);
    }

}

