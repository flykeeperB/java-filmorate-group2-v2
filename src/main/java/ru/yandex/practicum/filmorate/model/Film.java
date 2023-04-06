package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.Comparator;
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

    class MyComp implements Comparator<Film> {
        @Override
        public int compare(Film o1, Film o2) {
            return o1.getLikes().size() - o2.getLikes().size();
        }
    }

}
