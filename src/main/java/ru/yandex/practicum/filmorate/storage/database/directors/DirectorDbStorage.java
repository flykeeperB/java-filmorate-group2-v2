package ru.yandex.practicum.filmorate.storage.database.directors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository("directorDbStorage")
public class DirectorDbStorage implements DirectorStorage {
    private static final String SQL_GET_ALL_DIRECTOR = "SELECT * FROM DIRECTORS WHERE DIRECTOR_ID=?";
    private static final String SQL_UPDATE_DIRECTORS = "UPDATE DIRECTORS SET DIRECTOR_NAME=? WHERE DIRECTOR_ID=?";
    private static final String SQL_DELETE_DIRECTOR = "DELETE FROM DIRECTORS WHERE DIRECTOR_ID=?";
    private static final String SQL_GET_ALL_DIRECTORS = "SELECT * FROM DIRECTORS";
    private final JdbcTemplate jdbcTemplate;

    protected final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<Director> findAllDirectors() {
        return jdbcTemplate.query(SQL_GET_ALL_DIRECTORS, new DirectorMapper());
    }

    @Override
    public Director findDirectorById(Long directorId) {
        Director director = jdbcTemplate.query(SQL_GET_ALL_DIRECTOR,
                new Object[]{directorId}, new DirectorMapper()).stream().findAny().orElse(null);
        if (director == null) {
            throw new NotFoundException("Режиссёра c таким id нет");
        }
        return director;
    }

    @Override
    public Director addDirector(Director director) {
        SimpleJdbcInsert insertIntoDirector = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("DIRECTORS")
                .usingGeneratedKeyColumns("DIRECTOR_ID");
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("DIRECTOR_NAME", director.getName());
        Number newId = insertIntoDirector.executeAndReturnKey(parameters);
        director.setId(newId.longValue());

        return findDirectorById(newId.longValue());
    }

    @Override
    public Director updateDirector(Long directorId, Director director) {
        findDirectorById(directorId);
        jdbcTemplate.update(SQL_UPDATE_DIRECTORS, director.getName(), directorId);
        return findDirectorById(directorId);
    }

    @Override
    public void deleteDirector(Long id) {
        findDirectorById(id);
        jdbcTemplate.update(SQL_DELETE_DIRECTOR, id);
    }

}

