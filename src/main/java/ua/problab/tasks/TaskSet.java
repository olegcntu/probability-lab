/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.tasks;

import java.util.List;

/**
 * Згенерований набір варіантів із ключем відповідей.
 *
 * @param title           заголовок роботи
 * @param seed            код набору
 * @param level           рівень складності
 * @param tasksPerVariant завдань у варіанті
 * @param variants        варіанти
 */
public record TaskSet(String title, long seed, int level, int tasksPerVariant, List<Variant> variants) {

    public record Variant(int number, List<Item> tasks) {
    }

    /**
     * @param number        номер завдання
     * @param text          умова
     * @param answer        відповідь дробом
     * @param answerDecimal відповідь десятковим дробом
     * @param level         рівень складності завдання
     */
    public record Item(int number, String text, String answer, String answerDecimal, int level) {
    }
}
