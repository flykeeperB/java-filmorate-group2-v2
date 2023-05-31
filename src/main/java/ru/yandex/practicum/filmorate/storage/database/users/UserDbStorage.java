package ru.yandex.practicum.filmorate.storage.database.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {

    protected final JdbcTemplate jdbcTemplate;

    protected final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    protected static final String SQL_DELETE_USER = "DELETE FROM USERS WHERE USER_ID=?";
    protected static final String SQL_GET_USERS = "SELECT " +
            "u.USER_ID, " +
            "u.EMAIL, " +
            "u.LOGIN, " +
            "u.USER_NAME, " +
            "u.BIRTHDAY FROM USERS as u";

    protected static final String SQL_UPDATE_USER = "UPDATE USERS SET " +
            "EMAIL=?, " +
            "LOGIN=?, " +
            "USER_NAME=?, " +
            "BIRTHDAY=? " +
            "WHERE USER_ID=?";

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        namedParameterJdbcTemplate  = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public User addUser(User user) {
        SimpleJdbcInsert insertIntoUser = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("USERS")
                .usingGeneratedKeyColumns("USER_ID");

        final Map<String, Object> parameters = new HashMap<>();

        parameters.put("EMAIL", user.getEmail());
        parameters.put("LOGIN", user.getLogin());
        parameters.put("USER_NAME", user.getName());
        parameters.put("BIRTHDAY", user.getBirthday());

        Number newId = insertIntoUser.executeAndReturnKey(parameters);

        user.setId(newId.longValue());
        return findUserById(newId.longValue());
    }

    @Override
    public User updateUser(Long userId, User user) {

        findUserById(userId);

        try {
            jdbcTemplate.update(SQL_UPDATE_USER,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    userId);
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }

        return findUserById(userId);
    }

    private List<User> getUsers(List<Long> ids) {

        String query = SQL_GET_USERS;

        if (ids != null) {
            if (ids.isEmpty()) {
                return new ArrayList<>();
            } else {
                query += " WHERE u.USER_ID IN (:ids)";
            }
        }

        MapSqlParameterSource params = new MapSqlParameterSource();
        if (ids != null) {
            params.addValue("ids", ids);
        }

        try {
            return namedParameterJdbcTemplate.query(query, params, new UserMapper());
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }

        return new ArrayList<>();
    }

    @Override
    public List<User> findAllUser() {
        return getUsers(null);
    }

    @Override
    public User findUserById(Long userId) {

        List<User> users = getUsers(List.of(userId));

        if (users.isEmpty()) {
            throw new NotFoundException("Пользователя c таким id нет");
        }

        return users.get(0);
    }

    @Override
    public void deleteUserById(Long userId) {

        findUserById(userId);

        jdbcTemplate.update(SQL_DELETE_USER, userId);
    }
}
