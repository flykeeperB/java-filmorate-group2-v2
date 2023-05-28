package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.database.films.ExtraFunctionalFilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Service
public class ExtraFunctionalFilmService extends FilmService {
    private final ExtraFunctionalFilmDbStorage filmStorage;

    @Autowired
    public ExtraFunctionalFilmService(@Qualifier("extraFunctionalFilmDbStorage")
                                      ExtraFunctionalFilmDbStorage filmStorage) {
        super(filmStorage);
        this.filmStorage = filmStorage;
    }

    public List<Film> searchFilm(String query, String by) {
        return filmStorage.searchFilms(query, by);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getPopularFilms(Integer count, Long genreId, Integer year) {
        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public void addLike(long filmId, long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(long filmId, long userId) {
        filmStorage.deleteLike(filmId, userId);
    }

    public List<Film> getFilmsByDirectorSortByYear(long id) {
        return filmStorage.getFilmsByDirectorSortByYear(id);
    }

    public List<Film> getFilmsByDirectorSortByLikes(long id) {
        return filmStorage.getFilmsByDirectorSortByLikes(id);
    }
}
