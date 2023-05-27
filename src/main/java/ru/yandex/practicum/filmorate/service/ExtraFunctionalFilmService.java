package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.database.films.ExtraFunctionalFilmDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

@Service
public class ExtraFunctionalFilmService extends FilmService {
    private final ExtraFunctionalFilmDbStorage extraFunctionalFilmStorage;
    private final FilmStorage filmStorage;
    @Autowired
    public ExtraFunctionalFilmService(@Qualifier("extraFunctionalFilmDbStorage")
                                      ExtraFunctionalFilmDbStorage extraFunctionalFilmStorage,
                                      @Qualifier("filmDbStorage") FilmStorage filmStorage) {
        super(filmStorage);
        this.extraFunctionalFilmStorage = extraFunctionalFilmStorage;
        this.filmStorage = filmStorage;
    }

    public List<Film> searchFilm(String query, String by) {
        return extraFunctionalFilmStorage.searchFilms(query, by);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return extraFunctionalFilmStorage.getCommonFilms(userId, friendId);
    }
}
