package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Objects;

@Data
public class Director {
    private Long id;
    @NotBlank
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Director director = (Director) o;
        return id.equals(director.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
