/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.tasks;

import ua.problab.experiment.ExperimentConfig;
import ua.problab.math.Fraction;

import java.util.List;

/**
 * Згенероване обчислювальне завдання.
 *
 * @param templateId ідентифікатор шаблону
 * @param topic      тема (монети, кубики, урна, числа)
 * @param level      рівень складності 1–3
 * @param text       умова
 * @param answer     точна відповідь
 * @param hint       підказка після першої помилки
 * @param solution   кроки розв'язання (формули – у $…$)
 * @param lab        параметри експерименту для перевірки моделюванням (може бути null)
 */
public record GeneratedTask(
        String templateId,
        Topic topic,
        int level,
        String text,
        Fraction answer,
        String hint,
        List<String> solution,
        ExperimentConfig lab) {
}
