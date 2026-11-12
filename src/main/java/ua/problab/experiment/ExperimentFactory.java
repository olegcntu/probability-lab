/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

/** Створює експеримент за параметрами з панелі налаштувань і перевіряє їх коректність. */
public final class ExperimentFactory {

    private ExperimentFactory() {
    }

    public static Experiment create(ExperimentConfig cfg) {
        if (cfg == null || cfg.type() == null) {
            throw new IllegalArgumentException("Не вказано вид експерименту (монети, кубики або урна)");
        }
        return switch (cfg.type()) {
            case COINS -> new CoinsExperiment(cfg);
            case DICE -> new DiceExperiment(cfg);
            case URN -> new UrnExperiment(cfg);
        };
    }
}
