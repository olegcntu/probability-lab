/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

import java.util.List;

/**
 * Довідник для побудови панелі налаштувань: які події доступні для кожного експерименту
 * і що означає їхній числовий параметр.
 */
public final class ExperimentCatalog {

    /**
     * @param code       код події
     * @param label      підпис у списку
     * @param valueLabel підпис числового параметра (null — параметр не потрібен)
     */
    public record EventOption(EventKind code, String label, String valueLabel) {
    }

    /**
     * @param type   вид експерименту
     * @param title  назва
     * @param hint   коротка підказка
     * @param events доступні події
     */
    public record ExperimentInfo(ExperimentType type, String title, String hint, List<EventOption> events) {
    }

    public record Limits(int maxCoins, int maxStreak, int maxDice, int maxBalls, int maxDraw, int maxTrials) {
    }

    public record Catalog(List<ExperimentInfo> experiments, Limits limits) {
    }

    private ExperimentCatalog() {
    }

    public static Catalog catalog(int maxTrials) {
        return new Catalog(List.of(
                new ExperimentInfo(ExperimentType.COINS, "Монети",
                        "Кидання однієї або кількох монет. Можна зробити монету нечесною.",
                        List.of(
                                new EventOption(EventKind.HEADS_EXACTLY, "рівно k гербів", "k"),
                                new EventOption(EventKind.HEADS_AT_LEAST, "не менше k гербів", "k"),
                                new EventOption(EventKind.HEADS_AT_MOST, "не більше k гербів", "k"),
                                new EventOption(EventKind.COINS_ALL_SAME, "усі монети однаково", null),
                                new EventOption(EventKind.HEADS_AFTER_STREAK, "герб після серії з r гербів", "r"))),
                new ExperimentInfo(ExperimentType.DICE, "Кубики",
                        "Кидання від одного до чотирьох гральних кубиків.",
                        List.of(
                                new EventOption(EventKind.SUM_EQ, "сума очок дорівнює s", "s"),
                                new EventOption(EventKind.SUM_AT_LEAST, "сума очок не менша від s", "s"),
                                new EventOption(EventKind.SUM_AT_MOST, "сума очок не більша за s", "s"),
                                new EventOption(EventKind.SUM_EVEN, "сума очок парна", null),
                                new EventOption(EventKind.AT_LEAST_ONE_FACE, "хоча б раз випало v очок", "v"),
                                new EventOption(EventKind.DICE_ALL_SAME, "на всіх кубиках однаково", null))),
                new ExperimentInfo(ExperimentType.URN, "Урна",
                        "Виймання куль двох кольорів з поверненням або без повернення.",
                        List.of(
                                new EventOption(EventKind.RED_EXACTLY, "рівно m червоних куль", "m"),
                                new EventOption(EventKind.RED_AT_LEAST, "не менше m червоних куль", "m"),
                                new EventOption(EventKind.URN_ALL_SAME, "усі кулі одного кольору", null)))),
                new Limits(CoinsExperiment.MAX_COINS, CoinsExperiment.MAX_STREAK, DiceExperiment.MAX_DICE,
                        UrnExperiment.MAX_BALLS, UrnExperiment.MAX_DRAW, maxTrials));
    }
}
