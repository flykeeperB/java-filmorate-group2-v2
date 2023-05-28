package ru.yandex.practicum.filmorate.storage.database.directors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorStorage;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Repository
public class DirectorDbStorage implements DirectorStorage {
    static final String FIND_DIRECTOR_IN_TABLE_SQL = "SELECT * FROM LIST_OF_DIRECTORS WHERE DIRECTOR_ID=?";

    private final JdbcTemplate jdbcTemplate;

    protected final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private DirectorMapper directorMapper;

    @Autowired
    public DirectorDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public List<Director> findAllDirectors() {
        String sql = "SELECT * FROM LIST_OF_DIRECTORS";
        return jdbcTemplate.query(sql, directorMapper);
    }

    @Override
    public Director findDirectorById(long directorId) {
        Director director = jdbcTemplate.query(FIND_DIRECTOR_IN_TABLE_SQL,
                new Object[]{directorId}, directorMapper).stream().findAny().orElse(null);
        if (director == null) {
            throw new NotFoundException("Режиссёра c таким id нет");
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
    public Director updateDirector(long directorId, Director director) {
        Director directorExist = jdbcTemplate.query(FIND_DIRECTOR_IN_TABLE_SQL,
                new Object[]{directorId}, directorMapper).stream().findAny().orElse(null);
        if (directorExist == null) {
            throw new NotFoundException("Режиссёра c таким id нет");
        } else {
            String sqlQuery = "UPDATE LIST_OF_DIRECTORS SET DIRECTOR_NAME=? WHERE DIRECTOR_ID=?";
            jdbcTemplate.update(sqlQuery, director.getName(), directorId);
        }
        return director;
    }

    @Override
    public void deleteDirector(long id) {
        String sqlQuery = "DELETE FROM LIST_OF_DIRECTORS WHERE DIRECTOR_ID=?";

        jdbcTemplate.update(sqlQuery, id);
    }

}

