package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private long id;
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
    private Set<Long> friends = new HashSet<>();

}
