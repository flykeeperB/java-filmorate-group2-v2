package ru.yandex.practicum.filmorate.storage.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.StorageException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public final class DbConnector<T> {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public DbConnector(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    public <E> List<E> queryWithParamsByCustomType(String query,
                                                   MapSqlParameterSource params,
                                                   RowMapper<E> rowMapper) {
        try {
            log.info("Вызов запроса: " + query);
            return namedParameterJdbcTemplate.query(query, params, rowMapper);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw new StorageException("Ошибка базы данных при выполнении запроса.");
        }
    }

    public <E> Map<Long, List<E>> queryWithParamsByCustomTypeExtractor(String query,
                                                                       MapSqlParameterSource params,
                                                                       ResultSetExtractor<Map<Long, List<E>>> extractor) {
        try {
            log.info("Вызов запроса: " + query);
            return namedParameterJdbcTemplate.query(query, params, extractor);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
            throw new StorageException("Ошибка базы данных при выполнении запроса.");
        }
    }

    public List<T> queryWithParams(String query,
                                   MapSqlParameterSource params,
                                   RowMapper<T> rowMapper) {
        return this.queryWithParamsByCustomType(query, params, rowMapper);
    }

    public int runWithParams(String query,
                             MapSqlParameterSource params) {
        try {
            log.info("Вызов запроса: " + query);
            return namedParameterJdbcTemplate.update(query, params);
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            throw new StorageException("Идентификатор не задан.");
        }
    }

    public Long create(String table,
                       String generatedKeyColumns,
                       Map<String, Object> parameters) {

        SimpleJdbcInsert insertRequest = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName(table)
                .usingGeneratedKeyColumns(generatedKeyColumns);

        try {
            long result = insertRequest.executeAndReturnKey(parameters).longValue();
            log.info("В таблицу " + table + " добавлена запись id=" + result);
            return result;
        } catch (DataIntegrityViolationException e) {
            log.error(e.getMessage());
            throw new StorageException("Ошибка базы данных при добавлении записи.");
        }
    }

    public static void validateId(Long id) {
        if (id == null) {
            throw new ValidationException("Идентификатор не задан.");
        }
        if (id < 1) {
            throw new NotFoundException("Запись по неверному идентификатору не может быть найдена.");
        }
    }

}
