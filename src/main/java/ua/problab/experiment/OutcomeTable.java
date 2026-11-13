/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

import java.util.List;

/**
 * Наочне подання простору елементарних наслідків.
 * Для двох кубиків – таблиця 6×6, для кількох монет – перелік послідовностей.
 *
 * @param kind      "grid" (таблиця) або "list" (перелік)
 * @param caption   підпис
 * @param rowLabels підписи рядків (для таблиці)
 * @param colLabels підписи стовпців (для таблиці)
 * @param cells     вміст клітинок (для таблиці – рядками; для переліку – один рядок)
 * @param marks     чи сприяє клітинка події A
 */
public record OutcomeTable(
        String kind,
        String caption,
        List<String> rowLabels,
        List<String> colLabels,
        List<List<String>> cells,
        List<List<Boolean>> marks) {
}
