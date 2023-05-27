package ru.yandex.practicum.filmorate.storage.database;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.MpaNotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private MpaMapper mpaMapper;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> findAllMpas() {
        String sql = "SELECT * FROM LIST_OF_MPAS";
        return jdbcTemplate.query(sql, mpaMapper);
    }

    @Override
    public Mpa findMpaById(long mpaId) {
        Mpa mpa = new Mpa();
        try {
            String sql = "SELECT MPA_ID FROM LIST_OF_MPAS WHERE MPA_ID=?";
            boolean exists = false;
            int count = jdbcTemplate.queryForObject(sql, new Object[]{mpaId}, Integer.class);
            exists = count > 0;

            if (exists) {
                mpa = jdbcTemplate.query("SELECT * FROM LIST_OF_MPAS WHERE MPA_ID=?", new Object[]{mpaId}, mpaMapper)
                        .stream().findAny().orElse(null);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new MpaNotFoundException("Mpa c таким id нет");
        }
        return mpa;
    }
}
