package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Mpa {
    private long id;
    private String name;

    public Mpa() {
    }

    public Mpa(long id) {
        this.id = id;
    }
}
