package ru.yandex.practicum.filmorate.storage.database.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.ExtraFunctionalUserStorage;

import java.util.List;

@Component("extraFunctionalUserDbStorage")
@Repository
public class ExtraFunctionalUserDbStorage extends UserDbStorage implements ExtraFunctionalUserStorage {
    @Autowired
    public ExtraFunctionalUserDbStorage(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        User userExist = jdbcTemplate.query(FIND_USER_BY_ID_IN_TABLE_SQL,
                new Object[]{friendId}, new UserMapper()).stream().findAny().orElse(null);
        if (userExist == null) {
            throw new NotFoundException("Такого пользователя нет");
        } else {
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
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        String sql = "DELETE FROM FRIENDSHIP WHERE USER1_ID=? AND USER2_ID=?;";
        jdbcTemplate.update(sql, friendId, userId);
    }

    @Override
    public List<User> findAllFriends(long userId) {
        User userExist = jdbcTemplate.query(FIND_USER_BY_ID_IN_TABLE_SQL,
                new Object[]{userId}, new UserMapper()).stream().findAny().orElse(null);
        if (userExist == null) {
            throw new NotFoundException("Такого пользователя нет");
        } else {
            String sql = "SELECT * FROM USERS WHERE USER_ID IN (SELECT USER1_ID FROM FRIENDSHIP WHERE USER2_ID=?)";
            return jdbcTemplate.query(sql, new Object[]{userId}, new UserMapper());
        }
    }

    @Override
    public List<User> findCommonFriends(long userId, long otherUserId) {
        String sql = "SELECT * FROM USERS WHERE USER_ID=" +
                "(SELECT * FROM(SELECT USER1_ID FROM FRIENDSHIP WHERE USER2_ID=? " +
                "UNION ALL SELECT USER1_ID FROM FRIENDSHIP WHERE USER2_ID=?) GROUP BY USER1_ID HAVING COUNT(USER1_ID)=2)";
        return jdbcTemplate.query(sql, new Object[]{userId, otherUserId}, new UserMapper());
    }
}
