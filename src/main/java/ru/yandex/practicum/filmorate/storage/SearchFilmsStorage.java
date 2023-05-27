package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface SearchFilmsStorage {
    List<Film> searchFilms(String query, String by);
}
