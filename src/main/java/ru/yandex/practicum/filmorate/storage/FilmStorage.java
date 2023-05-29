package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    //методы добавления, удаления и модификации объектов.
    Film addFilm(Film film);

    Film updateFilm(Long id, Film film);

    List<Film> findAllFilms();

    List<Film> getFilms(List<Long> ids);

    List<Film> getFilms(List<Long> ids, String orderFields);

    Film findFilmById(Long id);

    List<Film> getPopularFilms(Long count);

    void deleteLike(Long filmId, Long userId);

    void addLike(Long filmId, Long userId);

    void deleteFilmById(Long filmId);
}
