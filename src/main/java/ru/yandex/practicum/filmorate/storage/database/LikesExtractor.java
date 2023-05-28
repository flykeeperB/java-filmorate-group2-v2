package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class LikesExtractor implements ResultSetExtractor<Map<Long, Long>> {
    @Override
    public Map<Long, Long> extractData(ResultSet rs) throws SQLException, DataAccessException {
        Map<Long, Long> data = new HashMap<>();
        while (rs.next()) {
            data.put(rs.getLong("FILM_ID"), rs.getLong("COUNT_LIKES"));

        }
        return data;
    }
}
