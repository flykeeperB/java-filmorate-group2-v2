package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    //методы добавления, удаления и модификации объектов.
    Film addFilm(Film film);
    
    Film updateFilm(long id, Film film);

    List<Film> findAllFilms();

    Film findFilmById(long id);

    List<Film> getPopularFilms(int count);

    Film deleteLike(long filmId, long userId);

    Film addLike(long filmId, long userId);
}
