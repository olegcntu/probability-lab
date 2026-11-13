/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.practice;

import java.util.List;

/**
 * Запитання для учня (без правильної відповіді).
 *
 * @param id      ідентифікатор для перевірки
 * @param level   рівень складності
 * @param topic   тема
 * @param kind    "NUMBER" – відповідь числом; "CHOICE" – вибір варіанта
 * @param text    умова
 * @param choices варіанти відповіді (для CHOICE)
 * @param hasLab  чи можна перевірити відповідь експериментом
 */
public record PracticeQuestion(String id, int level, String topic, String kind, String text, List<String> choices, boolean hasLab) {
}
