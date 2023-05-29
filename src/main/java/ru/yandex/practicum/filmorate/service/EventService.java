package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.EventStorage;
import ru.yandex.practicum.filmorate.storage.ExtraFunctionalUserStorage;

import java.util.List;

@Service
public class EventService {

    private final ExtraFunctionalUserStorage userStorage;
    private final EventStorage eventStorage;

    @Autowired
    public EventService(@Qualifier("extraFunctionalUserDbStorage") ExtraFunctionalUserStorage userStorage,
                        EventStorage eventStorage) {
        this.userStorage = userStorage;
        this.eventStorage = eventStorage;
    }

    public List<Event> getAllByIdUser(long userId) {
        userStorage.findUserById(userId);
        return eventStorage.getEventById(userId);
    }

}
