package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
public class User {
    private int id;
    @Email
    @NotBlank
    @NotEmpty
    private String email;
    @NotBlank
    @NotEmpty
    private String login;
    private String name;
    @NotBlank
    @NotEmpty
    @Past
    private LocalDate birthday;

}
