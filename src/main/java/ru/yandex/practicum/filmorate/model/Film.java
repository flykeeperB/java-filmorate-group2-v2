package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private long id;
    @NotBlank
    @NotEmpty
    private String name;
    @NotBlank
    @NotEmpty
    @Size(max = 200)
    private String description;
    @NotBlank
    @NotEmpty
    private LocalDate releaseDate;
    @NotBlank
    @NotEmpty
    @Positive
    private int duration;
    private Set<Long> likes = new HashSet<>();

}
