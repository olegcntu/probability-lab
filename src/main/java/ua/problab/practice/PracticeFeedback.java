/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.practice;

import ua.problab.experiment.ExperimentConfig;

import java.util.List;

/**
 * Миттєвий зворотний зв'язок.
 *
 * @param correct  чи правильна відповідь
 * @param counted  чи зараховано спробу (false — відповідь не вдалося розпізнати)
 * @param expected правильна відповідь (показується після правильної відповіді або другої спроби)
 * @param message  повідомлення для учня
 * @param hint     підказка (після першої помилки)
 * @param solution кроки розв'язання (формули — у $…$)
 * @param lab      експеримент для перевірки відповіді моделюванням
 */
public record PracticeFeedback(boolean correct, boolean counted, String expected, String message, String hint,
                               List<String> solution, ExperimentConfig lab) {
}
