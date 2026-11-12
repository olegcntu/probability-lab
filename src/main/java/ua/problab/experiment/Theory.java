/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

import java.util.List;

/**
 * Теоретичний розв'язок: точна ймовірність події та покрокове пояснення («Поясни мені»).
 *
 * @param experimentText   опис випробування
 * @param eventText        опис події A
 * @param probability      точне значення у вигляді дробу, наприклад "3/8"
 * @param probabilityTex   той самий дріб у розмітці формул
 * @param decimal          десятковий запис з комою, наприклад "0,375"
 * @param approxSign       "=" для точного десяткового запису, "≈" — для наближеного
 * @param value            значення як число (для графіків)
 * @param favorable        кількість сприятливих наслідків m (null, якщо наслідки не рівноможливі)
 * @param total            загальна кількість рівноможливих наслідків n (або null)
 * @param method           яким способом знайдено ймовірність
 * @param steps            кроки пояснення
 * @param outcomes         наочне подання простору наслідків (може бути null)
 * @param distribution     розподіл статистики, яку показує гістограма
 */
public record Theory(
        String experimentText,
        String eventText,
        String probability,
        String probabilityTex,
        String decimal,
        String approxSign,
        double value,
        String favorable,
        String total,
        String method,
        List<ExplainStep> steps,
        OutcomeTable outcomes,
        Distribution distribution) {
}
