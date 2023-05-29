package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.service.EventService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/users")
public class EventController {

    private final EventService eventService;

    @GetMapping("/{id}/feed")
    public List<Event> getEventsByIdUser(@PathVariable("id") Long userId) {
        log.info("GET: /users/{}/feed", userId);
        return eventService.getAllByIdUser(userId);
    }
}
