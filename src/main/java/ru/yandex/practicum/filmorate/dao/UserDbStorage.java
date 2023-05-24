package ru.yandex.practicum.filmorate.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//класс DAO - объект доступа к данным. Необходимо написать
// соответствующие мапперы и методы, позволяющие сохранять
// пользователей в БД и получать их из неё

@Component("userDbStorage")
@Repository
public class UserDbStorage implements UserStorage {

    public static final String FIND_USER_BY_ID_IN_TABLE_SQL = "SELECT USER_ID FROM USERS WHERE USER_ID=?";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private UserMapper userMapper;

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
    public User updateUser(long id, User user) {
        try {
            boolean exists = false;
            int count = jdbcTemplate.queryForObject(FIND_USER_BY_ID_IN_TABLE_SQL, new Object[]{id}, Integer.class);
            exists = count > 0;

            if (exists) {
                String sqlQuery = "UPDATE USERS SET EMAIL=?, LOGIN=?, USER_NAME=?, BIRTHDAY=? WHERE USER_ID=?";
                jdbcTemplate.update(sqlQuery,
                        user.getEmail(),
                        user.getLogin(),
                        user.getName(),
                        user.getBirthday(),
                        id);
            }
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Такого пользователя нет");
        }

        return user;
    }

    @Override
    public List<User> findAllUser() {
        String sql = "SELECT * FROM USERS";
        return jdbcTemplate.query(sql, userMapper);
    }

    @Override
    public User findUserById(long id) {
        User user = new User();
        try {
            String sql = "SELECT USER_ID FROM USERS WHERE USER_ID=?";
            boolean exists = false;
            int count = jdbcTemplate.queryForObject(sql, new Object[]{id}, Integer.class);
            exists = count > 0;

            if (exists) {
                user = jdbcTemplate.query("SELECT * FROM USERS WHERE USER_ID=?", new Object[]{id}, userMapper)
                        .stream().findAny().orElse(null);
            }

        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Такого пользователя нет");
        }
        return user;
    }

    @Override
    public void addFriend(long userId, long friendId) {
        try {
            boolean exists = false;
            int count2 = jdbcTemplate.queryForObject(FIND_USER_BY_ID_IN_TABLE_SQL, new Object[]{friendId}, Integer.class);
            exists = count2 > 0;

            if (exists) {
                String sqlQuery = "SELECT count(*) FROM FRIENDSHIP where USER1_ID=? AND USER2_ID=?";
                boolean exists2 = false;
                int count3 = jdbcTemplate.queryForObject(sqlQuery, new Object[]{userId, friendId}, Integer.class);
                int count4 = jdbcTemplate.queryForObject(sqlQuery, new Object[]{friendId, userId}, Integer.class);
                exists2 = count3 > 0 || count4 > 0;
                if (exists2 == false) {
                    jdbcTemplate.update("INSERT INTO FRIENDSHIP (USER1_ID, USER2_ID, STATUS) VALUES (?, ?, ?)", friendId, userId, "unconfirmed");
                } else if (count3 > 0) {
                    jdbcTemplate.update("UPDATE FRIENDSHIP SET STATUS = ? WHERE USER2_ID=? AND USER1_ID=?", "confirmed", friendId, userId);
                } else if (count4 > 0) {
                    jdbcTemplate.update("UPDATE FRIENDSHIP SET STATUS = ? WHERE USER2_ID=? AND USER1_ID=?", "confirmed", userId, friendId);
                }
            }
        } catch (EmptyResultDataAccessException e) {
            throw new UserNotFoundException("Такого пользователя нет");
        }
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        String sql = "DELETE FROM FRIENDSHIP WHERE USER1_ID=? AND USER2_ID=?;";
        jdbcTemplate.update(sql, friendId, userId);
    }

    @Override
    public List<User> findAllFriends(long userId) {
        String sql = "SELECT * FROM USERS WHERE USER_ID IN (SELECT USER1_ID FROM FRIENDSHIP WHERE USER2_ID=?)";
        return jdbcTemplate.query(sql, new Object[]{userId}, userMapper);
    }

    @Override
    public List<User> findCommonFriends(long userId, long otherUserId) {
        String sql = "SELECT * FROM USERS WHERE USER_ID=" +
                "(SELECT * FROM(SELECT USER1_ID FROM FRIENDSHIP WHERE USER2_ID=? " +
                "UNION ALL SELECT USER1_ID FROM FRIENDSHIP WHERE USER2_ID=?) GROUP BY USER1_ID HAVING COUNT(USER1_ID)=2)";
        return jdbcTemplate.query(sql, new Object[]{userId, otherUserId}, userMapper);
    }
}
