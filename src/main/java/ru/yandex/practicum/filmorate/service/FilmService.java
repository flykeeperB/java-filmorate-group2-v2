package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.CommonFilmsStorage;
import ru.yandex.practicum.filmorate.storage.FilmWithSearchStorage;

import java.util.List;

@Service
public class FilmService {
    private final FilmWithSearchStorage filmStorage;
    private final CommonFilmsStorage commonFilmsStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmWithSearchStorage filmStorage,
                       @Qualifier("filmDbStorage") CommonFilmsStorage commonFilmsStorage) {
        this.filmStorage = filmStorage;
        this.commonFilmsStorage = commonFilmsStorage;
    }

    public void addLike(long filmId, long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getAllFilms() {
        return filmStorage.findAllFilms();
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getPopularFilms(count);
    }

    public Film createFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(long id, Film film) {
        return filmStorage.updateFilm(id, film);
    }

    public Film getFilmById(long filmId) {
        return filmStorage.findFilmById(filmId);
    }

    public List<Film> searchFilm(String query, String by) {
        return filmStorage.searchFilms(query, by);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return commonFilmsStorage.getCommonFilms(userId, friendId);
    }
}
