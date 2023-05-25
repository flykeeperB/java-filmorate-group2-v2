package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmWithSearchStorage extends FilmStorage {
    List<Film> searchFilms(String query, String by);
}
