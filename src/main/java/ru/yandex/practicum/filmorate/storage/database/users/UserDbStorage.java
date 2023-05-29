package ru.yandex.practicum.filmorate.storage.database.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("userDbStorage")
@Repository
public class UserDbStorage implements UserStorage {

    static final String FIND_USER_BY_ID_IN_TABLE_SQL = "SELECT * FROM USERS WHERE USER_ID=?";

    protected final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
    public User updateUser(long userId, User user) {
        if (!contains(userId)) {
            throw new NotFoundException("Такого пользователя нет");
        } else {
            String sqlQuery = "UPDATE USERS SET EMAIL=?, LOGIN=?, USER_NAME=?, BIRTHDAY=? WHERE USER_ID=?";
            jdbcTemplate.update(sqlQuery,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    user.getBirthday(),
                    userId);
        }
        return user;
    }

    @Override
    public List<User> findAllUser() {
        String sql = "SELECT * FROM USERS";
        return jdbcTemplate.query(sql, new UserMapper());
    }

    @Override
    public User findUserById(long userId) {
        User user;
        if (!contains(userId)) {
            throw new NotFoundException("Такого пользователя нет");
        } else {
            user = jdbcTemplate.query("SELECT * FROM USERS WHERE USER_ID=?", new Object[]{userId}, new UserMapper())
                    .stream().findAny().orElse(null);
        }
        return user;
    }

    @Override
    public boolean contains(long id) {
        User userExist = jdbcTemplate.query(FIND_USER_BY_ID_IN_TABLE_SQL,
                new Object[]{id}, new UserMapper()).stream().findAny().orElse(null);

        return userExist != null;
    }

}
