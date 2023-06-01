package ru.yandex.practicum.filmorate.storage.database.mparatings;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaMapper implements RowMapper<Mpa> {
    @Override
    public Mpa mapRow(ResultSet rs, int rowNum) throws SQLException {
        Mpa mpa = new Mpa();

        mpa.setId(rs.getLong("MPA_ID"));
        mpa.setName(rs.getString("MPA_NAME"));

        return mpa;
    }
}
