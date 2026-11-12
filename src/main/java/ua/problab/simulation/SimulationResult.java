/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.simulation;

import ua.problab.experiment.ExperimentConfig;

import java.util.List;

/**
 * Результат моделювання.
 *
 * @param config       нормалізовані параметри експерименту
 * @param trials       кількість випробувань N
 * @param successes    у скількох випробуваннях відбулася подія A (M)
 * @param frequency    відносна частота W(A) = M / N
 * @param points       контрольні точки [i, кількість успіхів після i випробувань]
 * @param histogram    скільки разів статистика набула кожного значення
 * @param histogramMin значення статистики, що відповідає першому елементу гістограми
 * @param lastOutcomes останні наслідки для показу на екрані
 * @param seed         зерно генератора — дає змогу точно повторити серію
 */
public record SimulationResult(
        ExperimentConfig config,
        int trials,
        long successes,
        double frequency,
        List<long[]> points,
        long[] histogram,
        int histogramMin,
        List<Outcome> lastOutcomes,
        long seed) {

    /** Наслідок одного випробування: символи для показу і чи відбулася подія A. */
    public record Outcome(List<String> tokens, boolean success) {
    }
}
