package ru.yandex.practicum.filmorate.storage.database.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
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

    private static final String SQL_GET_COUNT_FROM_FRIENDSHIP =
            "SELECT count(*) FROM FRIENDSHIP where USER1_ID=? AND USER2_ID=?";

    private static final String SQL_INSERT_FRIENDSHIP =
            "INSERT INTO FRIENDSHIP (USER1_ID, USER2_ID, STATUS) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_FRIENDSHIP = "UPDATE FRIENDSHIP SET STATUS = ? WHERE USER2_ID=? ";
    private static final String SQL_DELETE_FRIENDSHIP = "DELETE FROM FRIENDSHIP WHERE USER1_ID=? AND USER2_ID=?";
    private static final String SQL_GET_ALL_FRIENDS =
            "SELECT * FROM USERS WHERE USER_ID IN (SELECT USER1_ID FROM FRIENDSHIP WHERE USER2_ID=?)";
    private static final String SQL_GET_ALL_COMMON_FRIENDS = "SELECT * FROM USERS WHERE USER_ID=" +
            "(SELECT * FROM(SELECT USER1_ID FROM FRIENDSHIP WHERE USER2_ID=? " +
            "UNION ALL SELECT USER1_ID FROM FRIENDSHIP WHERE USER2_ID=?) " +
            "GROUP BY USER1_ID HAVING COUNT(USER1_ID)=2)";

    @Override
    public void addFriend(long userId, long friendId) {

        findUserById(userId);
        findUserById(friendId);

        boolean exists2 = false;
        int count3 = jdbcTemplate.queryForObject(SQL_GET_COUNT_FROM_FRIENDSHIP, new Object[]{userId, friendId}, Integer.class);
        int count4 = jdbcTemplate.queryForObject(SQL_GET_COUNT_FROM_FRIENDSHIP, new Object[]{friendId, userId}, Integer.class);
        exists2 = count3 > 0 || count4 > 0;
        if (exists2 == false) {
            jdbcTemplate.update(SQL_INSERT_FRIENDSHIP,
                    friendId, userId, "unconfirmed");
        } else if (count3 > 0) {
            jdbcTemplate.update(SQL_UPDATE_FRIENDSHIP +
                    "AND USER1_ID=?", "confirmed", friendId, userId);
        } else if (count4 > 0) {
            jdbcTemplate.update(SQL_UPDATE_FRIENDSHIP +
                    "AND USER1_ID=?", "confirmed", userId, friendId);
        }
    }

    @Override
    public void deleteFriend(long userId, long friendId) {

        findUserById(userId);
        findUserById(friendId);

        jdbcTemplate.update(SQL_DELETE_FRIENDSHIP, friendId, userId);
    }

    @Override
    public List<User> findAllFriends(long userId) {

        findUserById(userId);

        return jdbcTemplate.query(SQL_GET_ALL_FRIENDS, new Object[]{userId}, new UserMapper());
    }

    @Override
    public List<User> findCommonFriends(long userId, long otherUserId) {

        findUserById(userId);
        findUserById(otherUserId);

        return jdbcTemplate.query(SQL_GET_ALL_COMMON_FRIENDS, new Object[]{userId, otherUserId}, new UserMapper());
    }
}
