/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

import java.util.List;

/**
 * Теоретичний розподіл статистики, яку фіксуємо в кожному випробуванні
 * (кількість гербів, сума очок, кількість червоних куль тощо).
 *
 * @param statisticName назва статистики для підпису осі
 * @param min           найменше значення статистики (зсув для гістограми)
 * @param labels        підписи значень
 * @param probabilities ймовірності значень
 * @param fractions     ті самі ймовірності у вигляді дробів
 * @param inEvent       чи входить значення до події A
 */
public record Distribution(
        String statisticName,
        int min,
        List<String> labels,
        List<Double> probabilities,
        List<String> fractions,
        List<Boolean> inEvent) {
}
