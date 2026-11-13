/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.simulation;

import org.junit.jupiter.api.Test;
import ua.problab.experiment.EventKind;
import ua.problab.experiment.ExperimentConfig;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SimulationServiceTest {

    private final SimulationService service = new SimulationService();

    @Test
    void sameSeedGivesSameSeries() {
        ExperimentConfig cfg = ExperimentConfig.dice(2, EventKind.SUM_EQ, 7);
        SimulationResult a = service.simulate(cfg, 5000, 42L);
        SimulationResult b = service.simulate(cfg, 5000, 42L);
        assertThat(a.successes()).isEqualTo(b.successes());
        assertThat(Arrays.equals(a.histogram(), b.histogram())).isTrue();
    }

    @Test
    void frequencyApproachesProbability() {
        // закон великих чисел: при 200 000 випробувань відхилення частоти від 1/6 мале
        SimulationResult r = service.simulate(ExperimentConfig.dice(2, EventKind.SUM_EQ, 7), 200_000, 7L);
        assertThat(r.frequency()).isCloseTo(1.0 / 6, within(0.01));
        SimulationResult u = service.simulate(ExperimentConfig.urn(5, 3, 3, false, EventKind.RED_EXACTLY, 2), 200_000, 9L);
        assertThat(u.frequency()).isCloseTo(15.0 / 28, within(0.01));
        SimulationResult s = service.simulate(ExperimentConfig.coins(1, EventKind.HEADS_AFTER_STREAK, 4), 50_000, 3L);
        assertThat(s.frequency()).isCloseTo(0.5, within(0.015));
    }

    @Test
    void histogramCountsAllTrialsAndPointsEndAtN() {
        SimulationResult r = service.simulate(ExperimentConfig.coins(5, EventKind.HEADS_AT_LEAST, 3), 12_345, 1L);
        assertThat(Arrays.stream(r.histogram()).sum()).isEqualTo(12_345);
        long[] last = r.points().get(r.points().size() - 1);
        assertThat(last[0]).isEqualTo(12_345);
        assertThat(last[1]).isEqualTo(r.successes());
        assertThat(r.lastOutcomes()).hasSize(30);
        assertThat(r.points().size()).isLessThan(800);
    }

    @Test
    void checkpointsAreIncreasing() {
        int[] cp = SimulationService.checkpoints(100_000);
        for (int i = 1; i < cp.length; i++) {
            assertThat(cp[i]).isGreaterThan(cp[i - 1]);
        }
        assertThat(cp[cp.length - 1]).isEqualTo(100_000);
    }
}
