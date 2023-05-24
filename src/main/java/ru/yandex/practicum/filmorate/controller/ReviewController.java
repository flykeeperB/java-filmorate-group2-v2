package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/reviews")
public class ReviewController {

    ReviewService service;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.service = reviewService;
    }

    @PostMapping
    public Review create(@Valid @RequestBody Review review) {
        log.info("POST: /reviews");
        return service.create(review);
    }

    @GetMapping("/{id}")
    public Review get(@PathVariable Long id) {
        log.info("GET: /reviews/" + id);
        return service.get(id);
    }

    @GetMapping()
    public List<Review> getAll(@RequestParam(defaultValue = "-1") Long filmId, @RequestParam(defaultValue = "10") Long count) {
        log.info("GET: /reviews");
        if (filmId < 1) {
            log.info("GET: /reviews all");
            return new ArrayList<>(service.getAll(count));
        }
        log.info("GET: /reviews byFilmId");
        return new ArrayList<>(service.getByFilmId(filmId, count));
    }

    @PutMapping
    public Review update(@Valid @RequestBody Review review) {
        log.info("PUT: /reviews");
        return service.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("DELETE: /reviews");
        service.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("PUT: /reviews/" + id + "/like/" + userId);
        service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("DELETE: /reviews/" + id + "/like/" + userId);
        service.deleteLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDisike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("PUT: /reviews/" + id + "/dislike/" + userId);
        service.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void deleteDislike(@PathVariable Integer id, @PathVariable Integer userId) {
        log.info("DELETE: /reviews/" + id + "/dislike/" + userId);
        service.deleteDislike(id, userId);
    }

}
