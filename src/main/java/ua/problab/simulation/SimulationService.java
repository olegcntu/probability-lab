/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.simulation;

import org.springframework.stereotype.Service;
import ua.problab.experiment.Experiment;
import ua.problab.experiment.ExperimentConfig;
import ua.problab.experiment.ExperimentFactory;
import ua.problab.experiment.Trial;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.SplittableRandom;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Моделювання серії незалежних випробувань методом Монте-Карло.
 * Для графіка збіжності зберігаються «контрольні точки»: після i-го випробування –
 * накопичена кількість успіхів. Кількість точок обмежена, тому відповідь компактна
 * навіть для 200 000 випробувань.
 */
@Service
public class SimulationService {

    public static final int MAX_TRIALS = 200_000;
    static final int LAST_OUTCOMES = 30;

    public SimulationResult simulate(ExperimentConfig config, int trials, Long seed) {
        if (trials < 1 || trials > MAX_TRIALS) {
            throw new IllegalArgumentException("Кількість випробувань має бути від 1 до " + MAX_TRIALS);
        }
        Experiment experiment = ExperimentFactory.create(config);
        long usedSeed = seed != null ? seed : ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        SplittableRandom rnd = new SplittableRandom(usedSeed);

        int min = experiment.statisticMin();
        long[] histogram = new long[experiment.statisticMax() - min + 1];
        int[] checkpoints = checkpoints(trials);
        List<long[]> points = new ArrayList<>(checkpoints.length);
        Deque<SimulationResult.Outcome> last = new ArrayDeque<>();
        long successes = 0;
        int next = 0;
        for (int i = 1; i <= trials; i++) {
            Trial t = experiment.sample(rnd);
            if (t.success()) {
                successes++;
            }
            histogram[t.statistic() - min]++;
            if (i > trials - LAST_OUTCOMES) {
                last.addLast(new SimulationResult.Outcome(experiment.tokens(t.raw()), t.success()));
            }
            if (next < checkpoints.length && checkpoints[next] == i) {
                points.add(new long[]{i, successes});
                next++;
            }
        }
        return new SimulationResult(experiment.config(), trials, successes, (double) successes / trials,
                points, histogram, min, new ArrayList<>(last), usedSeed);
    }

    /**
     * Номери випробувань, після яких фіксуємо частоту: перші 60 – усі, далі – геометрична
     * прогресія (для логарифмічної шкали) і рівномірна сітка (для лінійної шкали).
     */
    static int[] checkpoints(int n) {
        TreeSet<Integer> set = new TreeSet<>();
        for (int i = 1; i <= Math.min(n, 60); i++) {
            set.add(i);
        }
        double x = 60;
        while (x < n) {
            x *= 1.025;
            set.add((int) Math.min(n, Math.round(x)));
        }
        int linear = 250;
        for (int j = 1; j <= linear; j++) {
            set.add((int) Math.max(1, Math.round((double) n * j / linear)));
        }
        set.add(n);
        int[] result = new int[set.size()];
        int idx = 0;
        for (int v : set) {
            result[idx++] = v;
        }
        return result;
    }
}
