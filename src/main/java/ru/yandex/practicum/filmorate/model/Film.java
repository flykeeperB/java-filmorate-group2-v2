package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
public class Film {
    private int id;
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

}
