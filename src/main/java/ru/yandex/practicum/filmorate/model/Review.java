package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {
    private Long reviewId;

    @NotBlank
    private String content;

    @NotNull
    @JsonProperty("isPositive")
    private Boolean isPositive;

    @NotNull
    private Long userId;

    @NotNull
    private Long filmId;


    private Long useful;


}
