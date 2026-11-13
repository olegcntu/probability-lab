/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

/**
 * Один крок пояснення.
 *
 * @param title   короткий заголовок кроку
 * @param text    пояснення простими словами
 * @param formula формула у розмітці (\frac{a}{b}, ^{}, _{}, C_{n}^{k}) або null
 */
public record ExplainStep(String title, String text, String formula) {

    public static ExplainStep of(String title, String text) {
        return new ExplainStep(title, text, null);
    }

    public static ExplainStep of(String title, String text, String formula) {
        return new ExplainStep(title, text, formula);
    }
}
