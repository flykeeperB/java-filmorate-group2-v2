package ru.yandex.practicum.filmorate.exceptions;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
        log.error(message);
    }
}
