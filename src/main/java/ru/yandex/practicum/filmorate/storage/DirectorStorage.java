package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface DirectorStorage {
    List<Director> findAllDirectors();

    Director findDirectorById(Long directorId);

    Director addDirector(Director director);

    Director updateDirector(Long id, Director director);

    void deleteDirector(Long id);
}
