/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.tasks;

import org.junit.jupiter.api.Test;
import ua.problab.experiment.ExperimentFactory;
import ua.problab.math.Fraction;

import java.util.List;
import java.util.SplittableRandom;

import static org.assertj.core.api.Assertions.assertThat;

class TaskGeneratorTest {

    private final TaskGenerator generator = new TaskGenerator();

    @Test
    void sameCodeGivesSameVariants() {
        TaskSetRequest req = new TaskSetRequest(3, 5, 2, List.of(), 2026L, null);
        TaskSet a = generator.generate(req);
        TaskSet b = generator.generate(req);
        assertThat(a).isEqualTo(b);
        assertThat(a.variants()).hasSize(3);
        assertThat(a.variants().get(0).tasks()).hasSize(5);
    }

    @Test
    void variantsAreParallelButDifferent() {
        TaskSet set = generator.generate(new TaskSetRequest(4, 6, 3, List.of(), 77L, "Тест"));
        int different = 0;
        for (int i = 0; i < 6; i++) {
            String first = set.variants().get(0).tasks().get(i).text();
            String second = set.variants().get(1).tasks().get(i).text();
            // завдання з однаковим номером побудовані за одним шаблоном
            assertThat(first.split(" ")[0]).isEqualTo(second.split(" ")[0]);
            if (!first.equals(second)) {
                different++;
            }
        }
        assertThat(different).isGreaterThanOrEqualTo(4);
    }

    /** Кожен шаблон дає ймовірність з [0; 1], а експеримент для перевірки має ту саму теоретичну ймовірність. */
    @Test
    void everyTemplateIsConsistentWithItsExperiment() {
        SplittableRandom rnd = new SplittableRandom(5);
        for (TaskTemplates.Template t : TaskTemplates.ALL) {
            for (int i = 0; i < 40; i++) {
                GeneratedTask task = t.generator().apply(rnd);
                assertThat(task.answer().compareTo(Fraction.ZERO)).isGreaterThanOrEqualTo(0);
                assertThat(task.answer().compareTo(Fraction.ONE)).isLessThanOrEqualTo(0);
                assertThat(task.solution()).isNotEmpty();
                if (task.lab() != null) {
                    String theory = ExperimentFactory.create(task.lab()).theory().probability();
                    assertThat(theory).as(t.id() + ": " + task.text()).isEqualTo(task.answer().toString());
                }
            }
        }
    }
}
