package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.ExtraFunctionalFilmService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/films")
public class FilmController {
    private final ExtraFunctionalFilmService filmService;

    @Autowired
    public FilmController(ExtraFunctionalFilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public List<Film> findAll() {
        return filmService.getAllFilms();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @GetMapping("/popular")
    public List<Film> findPopularFilms(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count,
            @RequestParam(value = "genreId", defaultValue = "-1", required = false) Long genreId,
            @RequestParam(value = "year", defaultValue = "-1", required = false) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("POST request received: {}", film);
        if (film.getName() == null || film.getName().isBlank() || film.getName().isEmpty()) {
            log.error("Film name empty");
            throw new ValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            log.error("Film's description longer than 200 characters");
            throw new ValidationException("Описание фильма должно быть меньше 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Release date earlier than 12/28/1895");
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895.");
        }
        if (film.getDuration() < 0) {
            log.error("Duration of the film should not be negative");
            throw new ValidationException("Продолжительность фильма не должна быть отрицательной.");
        }
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film put(@RequestBody Film film) {
        log.info("PUT request received: {}", film);
        if (film.getName() == null || film.getName().isBlank() || film.getName().isEmpty()) {
            log.error("Film name empty");
            throw new ValidationException("Название фильма не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            log.error("Film's description longer than 200 characters");
            throw new ValidationException("Описание фильма должно быть меньше 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Release date earlier than 12/28/1895");
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895.");
        }
        if (film.getDuration() < 0) {
            log.error("Duration of the film should not be negative");
            throw new ValidationException("Продолжительность фильма не должна быть отрицательной.");
        }
        return filmService.updateFilm(film.getId(), film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void putLikeFilm(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId) {
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLikeFilm(
            @PathVariable("id") Long filmId,
            @PathVariable("userId") Long userId) {
        filmService.deleteLike(filmId, userId);
    }

    @GetMapping("/search")
    public List<Film> searchFilm(@RequestParam String query,
                                 @RequestParam String by) {
        return filmService.searchFilm(query, by);
    }

    @GetMapping("/common")
    public List<Film> searchFilm(@RequestParam Long userId,
                                 @RequestParam Long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(
            @PathVariable Long id) {
        filmService.deleteUserById(id);
    }
}
