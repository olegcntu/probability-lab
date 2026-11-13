/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.tasks;

import java.util.List;

/**
 * Налаштування генератора варіантів.
 *
 * @param variants        кількість варіантів (1–40)
 * @param tasksPerVariant завдань у варіанті (1–10)
 * @param level           рівень складності 1–3
 * @param topics          теми: COINS, DICE, URN, NUMBERS (порожньо – усі)
 * @param seed            код набору (зерно генератора) – для повторного отримання тих самих варіантів
 * @param title           заголовок роботи
 */
public record TaskSetRequest(Integer variants, Integer tasksPerVariant, Integer level, List<String> topics, Long seed, String title) {
}
