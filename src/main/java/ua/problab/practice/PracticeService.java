/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.practice;

import org.springframework.stereotype.Service;
import ua.problab.experiment.ExperimentConfig;
import ua.problab.math.Fmt;
import ua.problab.math.Fraction;
import ua.problab.tasks.GeneratedTask;
import ua.problab.tasks.TaskTemplates;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Режим практики для учнів: видає запитання і перевіряє відповіді на сервері.
 * Правильна відповідь зберігається лише в пам'яті сервера (під випадковим ідентифікатором),
 * тому її не видно в коді сторінки. Персональні дані не збираються.
 */
@Service
public class PracticeService {

    private static final Duration TTL = Duration.ofHours(3);
    private static final int MAX_STORED = 20_000;
    private static final List<String> PRAISE = List.of("Правильно!", "Чудово!", "Так, саме так!", "Відповідь правильна!");

    private record Issued(GeneratedTask task, ConceptQuestions.Concept concept, Instant created) {
    }

    private final Map<String, Issued> issued = new ConcurrentHashMap<>();

    public PracticeQuestion next(Integer level, Long seed) {
        int lvl = level == null ? 0 : Math.max(0, Math.min(3, level));
        SplittableRandom rnd = new SplittableRandom(seed != null ? seed : ThreadLocalRandom.current().nextLong());
        List<TaskTemplates.Template> templates = new ArrayList<>();
        for (TaskTemplates.Template t : TaskTemplates.ALL) {
            if (lvl == 0 || t.level() == lvl) {
                templates.add(t);
            }
        }
        List<ConceptQuestions.Concept> concepts = new ArrayList<>();
        for (ConceptQuestions.Concept c : ConceptQuestions.ALL) {
            if (lvl == 0 || c.level() == lvl) {
                concepts.add(c);
            }
        }
        cleanup();
        String id = UUID.randomUUID().toString();
        // приблизно кожне третє запитання — на розуміння понять
        boolean concept = !concepts.isEmpty() && rnd.nextInt(3) == 0;
        if (concept) {
            ConceptQuestions.Concept c = concepts.get(rnd.nextInt(concepts.size()));
            issued.put(id, new Issued(null, c, Instant.now()));
            return new PracticeQuestion(id, c.level(), "Поняття", "CHOICE", c.text(), c.choices(), c.lab() != null);
        }
        TaskTemplates.Template t = templates.get(rnd.nextInt(templates.size()));
        GeneratedTask task = t.generator().apply(rnd);
        issued.put(id, new Issued(task, null, Instant.now()));
        return new PracticeQuestion(id, task.level(), task.topic().title(), "NUMBER", task.text(), null, task.lab() != null);
    }

    public PracticeFeedback check(PracticeCheckRequest req) {
        Issued q = req == null || req.id() == null ? null : issued.get(req.id());
        if (q == null) {
            throw new IllegalArgumentException("Запитання не знайдено або воно застаріло. Натисніть «Наступне завдання».");
        }
        int attempt = req.attempt() == null ? 1 : Math.max(1, req.attempt());
        String praise = PRAISE.get(ThreadLocalRandom.current().nextInt(PRAISE.size()));

        if (q.concept() != null) {
            ConceptQuestions.Concept c = q.concept();
            int chosen;
            try {
                chosen = Integer.parseInt(req.answer() == null ? "" : req.answer().trim());
            } catch (NumberFormatException e) {
                return new PracticeFeedback(false, false, null, "Оберіть один з варіантів відповіді.", null, List.of(), null);
            }
            boolean ok = chosen == c.correct();
            String expected = c.choices().get(c.correct());
            if (ok) {
                return new PracticeFeedback(true, true, expected, praise + " " + c.explanation(), null, List.of(), c.lab());
            }
            if (attempt < 2) {
                return new PracticeFeedback(false, true, null, "Поки що ні. Подумайте ще раз або перевірте експериментом.",
                        "Підказка: уявіть, що випробування повторюють багато разів. Що відбуватиметься частіше?", List.of(), c.lab());
            }
            return new PracticeFeedback(false, true, expected, "Правильна відповідь: " + expected + ". " + c.explanation(), null, List.of(), c.lab());
        }

        GeneratedTask task = q.task();
        Optional<AnswerParser.Parsed> parsed = AnswerParser.parse(req.answer());
        if (parsed.isEmpty()) {
            return new PracticeFeedback(false, false, null,
                    "Не вдалося розпізнати відповідь. Запишіть її звичайним дробом (3/8), десятковим дробом (0,375) або у відсотках (37,5%).",
                    null, List.of(), null);
        }
        AnswerParser.Parsed a = parsed.get();
        Fraction expected = task.answer();
        String expectedText = expected + (expected.isInteger() ? "" : " " + Fmt.approx(expected));
        if (a.value().compareTo(Fraction.ZERO) < 0 || a.value().compareTo(Fraction.ONE) > 0) {
            return new PracticeFeedback(false, true, null,
                    "Ймовірність не може бути від'ємною або більшою за 1. Перевірте обчислення.", task.hint(), List.of(), task.lab());
        }
        if (AnswerParser.matches(a, expected)) {
            return new PracticeFeedback(true, true, expectedText, praise
                    + (task.lab() != null ? " Перевірте відповідь експериментом: частота має наблизитися до " + Fmt.decimal(expected) + "." : ""),
                    null, task.solution(), task.lab());
        }
        if (attempt < 2) {
            return new PracticeFeedback(false, true, null, "Поки що ні. Ось підказка — спробуйте ще раз.", task.hint(), List.of(), task.lab());
        }
        return new PracticeFeedback(false, true, expectedText, "Правильна відповідь: " + expectedText + ". Розгляньте розв'язання.",
                null, task.solution(), task.lab());
    }

    private void cleanup() {
        if (issued.size() < MAX_STORED / 2) {
            return;
        }
        Instant border = Instant.now().minus(TTL);
        issued.entrySet().removeIf(e -> e.getValue().created().isBefore(border));
        if (issued.size() > MAX_STORED) {
            issued.clear();
        }
    }

    /** Для тестів: правильна відповідь виданого запитання. */
    Optional<Fraction> answerOf(String id) {
        Issued q = issued.get(id);
        return q == null || q.task() == null ? Optional.empty() : Optional.of(q.task().answer());
    }

    Optional<ExperimentConfig> labOf(String id) {
        Issued q = issued.get(id);
        if (q == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(q.task() != null ? q.task().lab() : q.concept().lab());
    }
}
