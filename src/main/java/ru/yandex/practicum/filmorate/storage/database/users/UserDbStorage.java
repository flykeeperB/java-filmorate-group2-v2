package ru.yandex.practicum.filmorate.storage.database.users;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.storage.database.MainSqlQueryConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component("userDbStorage")
@Repository
public class UserDbStorage implements UserStorage {

    protected final JdbcTemplate jdbcTemplate;

    protected final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
        return user;
    }

    @Override
    public User updateUser(Long userId, User user) {

        findUserById(userId);

        String sqlQuery = "UPDATE USERS SET " +
                "EMAIL=?, " +
                "LOGIN=?, " +
                "USER_NAME=?, " +
                "BIRTHDAY=? " +
                "WHERE USER_ID=?";

        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                userId);

        return findUserById(userId);
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
    public List<User> getUsers(List<Long> ids) {

        MainSqlQueryConstructor queryConstructor = MainSqlQueryConstructor
                .builder()
                .fieldsPart(List.of(
                        "u.USER_ID",
                        "u.EMAIL",
                        "u.LOGIN",
                        "u.USER_NAME",
                        "u.BIRTHDAY"))
                .fromPart("USERS as u")
                .build();

        if (ids != null) {
            if (ids.isEmpty()) {
                return new ArrayList<>();
            } else {
                queryConstructor.setWherePart("u.USER_ID IN (:ids)");
            }
        }

        String query = queryConstructor.getSelectQuery();

        var params = new MapSqlParameterSource();
        if (ids != null) {
            params.addValue("ids", ids);
        }

        List<User> result = new ArrayList<>();

        try {
            result = namedParameterJdbcTemplate.query(query, params, new UserMapper());
        } catch (RuntimeException e) {
            log.error(e.getMessage());
        }

        return result;
    }

    @Override
    public void deleteUserById(Long userId) {

        findUserById(userId);

        String sql = "DELETE FROM USERS WHERE USER_ID=?";

        jdbcTemplate.update(sql, userId);
    }
}
