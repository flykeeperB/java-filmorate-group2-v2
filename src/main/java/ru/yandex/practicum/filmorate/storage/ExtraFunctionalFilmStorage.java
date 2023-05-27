package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface ExtraFunctionalFilmStorage extends FilmStorage {

    List<Film> searchFilms(String query, String by);

    List<Film> getCommonFilms(Long userId, Long friendId);
}
