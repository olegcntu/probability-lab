/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

/**
 * Параметри експерименту, які вчитель або учень задає на панелі налаштувань.
 *
 * @param type             вид експерименту
 * @param count            монети: кількість монет (1–10); кубики: кількість кубиків (1–4)
 * @param headsProbability монети: ймовірність герба, наприклад "1/2" або "0,3" (нечесна монета)
 * @param red              урна: кількість червоних куль
 * @param blue             урна: кількість синіх куль
 * @param draw             урна: скільки куль виймають (1–6)
 * @param replacement      урна: чи повертають кулю після кожного виймання
 * @param event            подія A
 * @param value            числовий параметр події (k, s, v, m або довжина серії r)
 */
public record ExperimentConfig(
        ExperimentType type,
        Integer count,
        String headsProbability,
        Integer red,
        Integer blue,
        Integer draw,
        Boolean replacement,
        EventKind event,
        Integer value) {

    public static ExperimentConfig coins(int count, EventKind event, Integer value) {
        return new ExperimentConfig(ExperimentType.COINS, count, null, null, null, null, null, event, value);
    }

    public static ExperimentConfig dice(int count, EventKind event, Integer value) {
        return new ExperimentConfig(ExperimentType.DICE, count, null, null, null, null, null, event, value);
    }

    public static ExperimentConfig urn(int red, int blue, int draw, boolean replacement, EventKind event, Integer value) {
        return new ExperimentConfig(ExperimentType.URN, null, null, red, blue, draw, replacement, event, value);
    }
}
