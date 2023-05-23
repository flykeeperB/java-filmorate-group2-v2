package ru.yandex.practicum.filmorate.dao;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewMapper implements RowMapper<Review> {
    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        Review review = Review.builder()
                .reviewId(rs.getLong("REVIEW_ID"))
                .userId(rs.getLong("USER_ID"))
                .filmId(rs.getLong("FILM_ID"))
                .content(rs.getString("CONTENT"))
                .isPositive(rs.getBoolean("IS_POSITIVE"))
                .useful(rs.getLong("USEFUL")).build();

        return review;
    }

}
