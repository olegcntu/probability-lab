/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.problab.tasks.TaskGenerator;
import ua.problab.tasks.TaskSet;
import ua.problab.tasks.TaskSetRequest;

/** Генерація варіантів самостійних і контрольних робіт. */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskGenerator generator;

    public TaskController(TaskGenerator generator) {
        this.generator = generator;
    }

    @PostMapping("/generate")
    public TaskSet generate(@RequestBody TaskSetRequest request) {
        return generator.generate(request == null ? new TaskSetRequest(null, null, null, null, null, null) : request);
    }
}
