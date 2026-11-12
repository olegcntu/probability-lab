/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.tasks;

import org.springframework.stereotype.Service;
import ua.problab.math.Fmt;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SplittableRandom;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;

/**
 * Генератор варіантів самостійної (контрольної) роботи.
 * Усі варіанти мають однакову структуру: завдання з тим самим номером побудоване за тим самим
 * шаблоном, але з іншими числами. Набір повністю визначається «кодом набору» (зерном генератора),
 * тож учитель може відтворити ті самі варіанти пізніше.
 */
@Service
public class TaskGenerator {

    public static final int MAX_VARIANTS = 40;
    public static final int MAX_TASKS = 10;

    public TaskSet generate(TaskSetRequest req) {
        int variants = clamp(req.variants(), 2, 1, MAX_VARIANTS);
        int perVariant = clamp(req.tasksPerVariant(), 5, 1, MAX_TASKS);
        int level = clamp(req.level(), 2, 1, 3);
        Set<Topic> topics = EnumSet.noneOf(Topic.class);
        if (req.topics() != null) {
            for (String t : req.topics()) {
                try {
                    topics.add(Topic.valueOf(t));
                } catch (IllegalArgumentException ignored) {
                    // невідома тема — пропускаємо
                }
            }
        }
        if (topics.isEmpty()) {
            topics = EnumSet.allOf(Topic.class);
        }
        long seed = req.seed() != null ? req.seed() : ThreadLocalRandom.current().nextLong(1000, 1_000_000);
        SplittableRandom rnd = new SplittableRandom(seed);

        List<TaskTemplates.Template> plan = plan(level, topics, perVariant, rnd);

        List<TaskSet.Variant> result = new ArrayList<>();
        Set<String> used = new HashSet<>();
        for (int v = 1; v <= variants; v++) {
            List<TaskSet.Item> items = new ArrayList<>();
            for (int i = 0; i < plan.size(); i++) {
                GeneratedTask task = null;
                for (int attempt = 0; attempt < 12; attempt++) {
                    task = plan.get(i).generator().apply(rnd);
                    if (used.add(task.text())) {
                        break;
                    }
                }
                items.add(new TaskSet.Item(i + 1, task.text(), task.answer().toString(), Fmt.decimal(task.answer()), task.level()));
            }
            result.add(new TaskSet.Variant(v, items));
        }
        String title = req.title() == null || req.title().isBlank() ? "Класичне означення ймовірності" : req.title().trim();
        return new TaskSet(title, seed, level, perVariant, result);
    }

    /**
     * Добирає послідовність шаблонів: від простіших до складніших, без повторів, доки вистачає шаблонів.
     */
    static List<TaskTemplates.Template> plan(int level, Set<Topic> topics, int count, RandomGenerator rnd) {
        List<TaskTemplates.Template> easy = new ArrayList<>();
        List<TaskTemplates.Template> main = new ArrayList<>();
        for (TaskTemplates.Template t : TaskTemplates.ALL) {
            if (!topics.contains(t.topic())) {
                continue;
            }
            if (t.level() == level) {
                main.add(t);
            } else if (t.level() == level - 1) {
                easy.add(t);
            }
        }
        if (main.isEmpty()) {
            for (TaskTemplates.Template t : TaskTemplates.ALL) {
                if (t.level() <= level) {
                    main.add(t);
                }
            }
        }
        shuffle(easy, rnd);
        shuffle(main, rnd);
        List<TaskTemplates.Template> plan = new ArrayList<>();
        // приблизно третина завдань — на рівень нижче (розминка), решта — обраного рівня
        int warmUp = level > 1 ? Math.min(easy.size(), count / 3) : 0;
        for (int i = 0; i < warmUp; i++) {
            plan.add(easy.get(i));
        }
        int idx = 0;
        while (plan.size() < count) {
            plan.add(main.get(idx % main.size()));
            idx++;
        }
        plan.sort((a, b) -> Integer.compare(a.level(), b.level()));
        return plan;
    }

    private static <T> void shuffle(List<T> list, RandomGenerator rnd) {
        for (int i = list.size() - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            T tmp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, tmp);
        }
    }

    private static int clamp(Integer value, int dflt, int min, int max) {
        int v = value == null ? dflt : value;
        return Math.max(min, Math.min(max, v));
    }
}
