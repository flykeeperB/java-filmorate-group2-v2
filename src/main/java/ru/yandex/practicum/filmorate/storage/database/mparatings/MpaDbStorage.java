package ru.yandex.practicum.filmorate.storage.database.mparatings;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@Repository
public class MpaDbStorage implements MpaStorage {
    private static final String SQL_GET_ALL_MPAS = "SELECT * FROM LIST_OF_MPAS";
    private static final String SQL_GET_MPA_ID = "SELECT MPA_ID FROM LIST_OF_MPAS WHERE MPA_ID=?";
    private static final String SQL_GET_ALL_MPA = "SELECT * FROM LIST_OF_MPAS WHERE MPA_ID=?";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private MpaMapper mpaMapper;

    @Autowired
    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> findAllMpas() {
        return jdbcTemplate.query(SQL_GET_ALL_MPAS, mpaMapper);
    }

    @Override
    public Mpa findMpaById(long mpaId) {
        Mpa mpa = new Mpa();
        try {
            boolean exists = false;
            int count = jdbcTemplate.queryForObject(SQL_GET_MPA_ID, new Object[]{mpaId}, Integer.class);
            exists = count > 0;

            if (exists) {
                mpa = jdbcTemplate.query(SQL_GET_ALL_MPA, new Object[]{mpaId}, mpaMapper)
                        .stream().findAny().orElse(null);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Mpa c таким id нет");
        }
        return mpa;
    }
}
