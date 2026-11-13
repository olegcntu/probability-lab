/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ua.problab.practice.PracticeCheckRequest;
import ua.problab.practice.PracticeFeedback;
import ua.problab.practice.PracticeQuestion;
import ua.problab.practice.PracticeService;

/** Режим практики: запитання та миттєва перевірка відповідей. */
@RestController
@RequestMapping("/api/practice")
public class PracticeController {

    private final PracticeService practice;

    public PracticeController(PracticeService practice) {
        this.practice = practice;
    }

    @GetMapping("/next")
    public PracticeQuestion next(@RequestParam(required = false) Integer level,
                                 @RequestParam(required = false) Long seed) {
        return practice.next(level, seed);
    }

    @PostMapping("/check")
    public PracticeFeedback check(@RequestBody PracticeCheckRequest request) {
        return practice.check(request);
    }
}
