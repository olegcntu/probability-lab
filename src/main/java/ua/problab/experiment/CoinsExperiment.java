/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

import ua.problab.math.Combinatorics;
import ua.problab.math.Fmt;
import ua.problab.math.Fraction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/** Кидання n монет (чесних або нечесних). */
final class CoinsExperiment implements Experiment {

    static final int MAX_COINS = 10;
    static final int MAX_STREAK = 6;

    private final ExperimentConfig cfg;
    private final int n;
    private final Fraction p;
    private final boolean fair;
    private final double pDouble;
    private final EventKind event;
    private final int value;
    private final boolean[] inEvent;

    CoinsExperiment(ExperimentConfig cfg) {
        this.event = cfg.event() == null ? EventKind.HEADS_EXACTLY : cfg.event();
        int count = cfg.count() == null ? 1 : cfg.count();
        if (event == EventKind.HEADS_AFTER_STREAK) {
            count = 1;
        }
        if (count < 1 || count > MAX_COINS) {
            throw new IllegalArgumentException("Кількість монет має бути від 1 до " + MAX_COINS);
        }
        this.n = count;
        this.p = parseProbability(cfg.headsProbability());
        this.fair = p.equals(Fraction.HALF);
        this.pDouble = p.toDouble();

        int v = cfg.value() == null ? defaultValue(event, n) : cfg.value();
        switch (event) {
            case HEADS_EXACTLY, HEADS_AT_LEAST, HEADS_AT_MOST -> {
                if (v < 0 || v > n) {
                    throw new IllegalArgumentException("Кількість гербів має бути від 0 до " + n);
                }
            }
            case COINS_ALL_SAME -> {
                if (n < 2) {
                    throw new IllegalArgumentException("Подія «усі монети однакові» має сенс, якщо монет щонайменше дві");
                }
            }
            case HEADS_AFTER_STREAK -> {
                if (v < 1 || v > MAX_STREAK) {
                    throw new IllegalArgumentException("Довжина серії гербів має бути від 1 до " + MAX_STREAK);
                }
            }
            default -> throw new IllegalArgumentException("Ця подія не стосується монет");
        }
        this.value = v;
        this.cfg = new ExperimentConfig(ExperimentType.COINS, n, p.toString(), null, null, null, null, event, v);

        if (event == EventKind.HEADS_AFTER_STREAK) {
            inEvent = new boolean[]{false, true};
        } else {
            inEvent = new boolean[n + 1];
            for (int k = 0; k <= n; k++) {
                inEvent[k] = switch (event) {
                    case HEADS_EXACTLY -> k == value;
                    case HEADS_AT_LEAST -> k >= value;
                    case HEADS_AT_MOST -> k <= value;
                    case COINS_ALL_SAME -> k == 0 || k == n;
                    default -> false;
                };
            }
        }
    }

    private static int defaultValue(EventKind event, int n) {
        return switch (event) {
            case HEADS_AFTER_STREAK -> 3;
            case COINS_ALL_SAME -> 0;
            default -> (n + 1) / 2;
        };
    }

    static Fraction parseProbability(String text) {
        if (text == null || text.isBlank()) {
            return Fraction.HALF;
        }
        Fraction f;
        try {
            String t = text.trim();
            if (t.contains("/")) {
                String[] parts = t.split("/");
                f = Fraction.of(Long.parseLong(parts[0].trim()), Long.parseLong(parts[1].trim()));
            } else {
                f = Fraction.ofDecimal(t);
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Ймовірність герба треба записати як дріб (1/2) або десятковий дріб (0,3)");
        }
        if (f.compareTo(Fraction.ZERO) <= 0 || f.compareTo(Fraction.ONE) >= 0) {
            throw new IllegalArgumentException("Ймовірність герба має бути більшою за 0 і меншою за 1");
        }
        return f;
    }

    @Override
    public ExperimentConfig config() {
        return cfg;
    }

    @Override
    public int statisticMin() {
        return 0;
    }

    @Override
    public int statisticMax() {
        return event == EventKind.HEADS_AFTER_STREAK ? 1 : n;
    }

    @Override
    public Trial sample(RandomGenerator rnd) {
        if (event == EventKind.HEADS_AFTER_STREAK) {
            int streak = 0;
            int guard = 0;
            while (streak < value && guard++ < 1_000_000) {
                streak = toss(rnd) ? streak + 1 : 0;
            }
            boolean next = toss(rnd);
            int[] raw = new int[value + 1];
            for (int i = 0; i < value; i++) {
                raw[i] = 1;
            }
            raw[value] = next ? 1 : 0;
            return new Trial(next, next ? 1 : 0, raw);
        }
        int[] raw = new int[n];
        int heads = 0;
        for (int i = 0; i < n; i++) {
            raw[i] = toss(rnd) ? 1 : 0;
            heads += raw[i];
        }
        return new Trial(inEvent[heads], heads, raw);
    }

    private boolean toss(RandomGenerator rnd) {
        return fair ? rnd.nextBoolean() : rnd.nextDouble() < pDouble;
    }

    @Override
    public List<String> tokens(int[] raw) {
        List<String> t = new ArrayList<>(raw.length);
        for (int r : raw) {
            t.add(r == 1 ? "Г" : "Ц");
        }
        return t;
    }

    // ------------------------------------------------------------------ теорія

    private Fraction pk(int k) {
        Fraction q = p.complement();
        return p.pow(k).multiply(q.pow(n - k)).multiply(Combinatorics.binomial(n, k));
    }

    @Override
    public Theory theory() {
        if (event == EventKind.HEADS_AFTER_STREAK) {
            return streakTheory();
        }
        Fraction prob = Fraction.ZERO;
        BigInteger favorable = BigInteger.ZERO;
        List<Integer> ks = new ArrayList<>();
        for (int k = 0; k <= n; k++) {
            if (inEvent[k]) {
                prob = prob.add(pk(k));
                favorable = favorable.add(Combinatorics.binomial(n, k));
                ks.add(k);
            }
        }
        BigInteger total = BigInteger.TWO.pow(n);
        String experimentText = n == 1 ? "Кидаємо одну монету"
                : "Кидаємо " + Fmt.count(n, "монету", "монети", "монет") + " (або одну монету " + n + " " + Fmt.plural(n, "раз", "рази", "разів") + ")";
        String eventText = eventText();

        List<ExplainStep> steps = new ArrayList<>();
        steps.add(ExplainStep.of("Випробування", experimentText + ". Подія A — " + eventText + "."));

        String method;
        if (fair) {
            method = "класичне означення ймовірності";
            steps.add(ExplainStep.of("Усі рівноможливі наслідки",
                    n == 1 ? "Монета падає гербом (Г) або цифрою (Ц) — 2 рівноможливі наслідки."
                            : "Кожна монета падає гербом (Г) або цифрою (Ц). Наслідок — це послідовність з " + n
                            + " букв, наприклад " + exampleSequence() + ". За правилом добутку кількість усіх наслідків:",
                    n == 1 ? "n = 2" : "n = 2^{" + n + "} = " + total));
            steps.add(favorableStep(ks, favorable));
            steps.add(ExplainStep.of("Ймовірність", "За класичним означенням ймовірність дорівнює відношенню кількості сприятливих наслідків до кількості всіх наслідків:",
                    "P(A) = " + Fmt.frac("m", "n") + " = " + Fmt.frac(favorable, total) + simplified(prob, favorable, total) + " " + Fmt.approx(prob)));
        } else {
            method = "формула Бернуллі (монета нечесна)";
            Fraction q = p.complement();
            steps.add(ExplainStep.of("Монета нечесна",
                    "Герб випадає з ймовірністю p = " + Fmt.decimal(p) + ", цифра — з ймовірністю q = 1 − p = " + Fmt.decimal(q)
                            + ". Послідовності Г і Ц тепер не рівноможливі, тому класичне означення не підходить. Кидки незалежні, отже ймовірність отримати рівно k гербів знаходимо за формулою Бернуллі:",
                    "P_{" + n + "}(k) = C_{" + n + "}^{k} \\cdot p^{k} \\cdot q^{" + n + " - k}"));
            StringBuilder sum = new StringBuilder("P(A) = ");
            for (int i = 0; i < ks.size(); i++) {
                int k = ks.get(i);
                if (i > 0) {
                    sum.append(" + ");
                }
                sum.append(Fmt.c(n, k)).append(" \\cdot ").append(Fmt.decimal(p)).append("^{").append(k).append("} \\cdot ")
                        .append(Fmt.decimal(q)).append("^{").append(n - k).append("}");
                if (ks.size() > 4 && i == 1) {
                    sum.append(" + …");
                    i = ks.size() - 2;
                }
            }
            sum.append(" ").append(Fmt.approx(prob));
            steps.add(ExplainStep.of("Обчислення", "Додаємо ймовірності всіх значень k, які сприяють події A:", sum.toString()));
        }
        steps.add(plainWords(prob));
        if (event == EventKind.HEADS_EXACTLY && n >= 2 && fair) {
            steps.add(ExplainStep.of("Зверніть увагу",
                    "Різна кількість гербів має різну ймовірність: наприклад, рівно " + (n / 2) + " " + Fmt.plural(n / 2, "герб", "герби", "гербів")
                            + " можна отримати " + Combinatorics.binomial(n, n / 2) + " способами, а " + n + " " + Fmt.plural(n, "герб", "герби", "гербів")
                            + " — лише одним. Тому наслідки «кількість гербів» не є рівноможливими."));
        }

        return new Theory(experimentText, eventText, prob.toString(), Fmt.frac(prob), Fmt.decimal(prob), Fmt.approx(prob).substring(0, 1),
                prob.toDouble(), fair ? favorable.toString() : null, fair ? total.toString() : null, method, steps,
                outcomeList(), distribution());
    }

    private String exampleSequence() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(i % 3 == 1 ? 'Ц' : 'Г');
        }
        return sb.toString();
    }

    private ExplainStep favorableStep(List<Integer> ks, BigInteger favorable) {
        String text;
        String formula;
        switch (event) {
            case HEADS_EXACTLY -> {
                if (n == 1) {
                    text = "Події A сприяє один наслідок: " + (value == 1 ? "Г" : "Ц") + ".";
                    formula = "m = 1";
                } else if (value == 0 || value == n) {
                    text = "Події A сприяє лише одна послідовність — усі монети впали " + (value == 0 ? "цифрою" : "гербом") + ".";
                    formula = "m = " + Fmt.c(n, value) + " = 1";
                } else {
                    text = "Події A сприяють ті послідовності, у яких герб стоїть рівно на " + value + " "
                            + Fmt.plural(value, "місці", "місцях", "місцях") + " з " + n
                            + ". Вибрати ці місця можна стількома способами, скільки є комбінацій з " + n + " по " + value + ":";
                    formula = "m = " + Fmt.c(n, value) + " = " + favorable;
                }
            }
            case HEADS_AT_LEAST, HEADS_AT_MOST -> {
                if (event == EventKind.HEADS_AT_LEAST && value == 1) {
                    text = "Зручно перейти до протилежної події Ā «не випало жодного герба» — їй сприяє лише одна послідовність (усі Ц). Тоді сприятливих для A наслідків:";
                    formula = "m = 2^{" + n + "} - 1 = " + favorable;
                } else {
                    text = "Подія A означає, що кількість гербів дорівнює " + joinKs(ks)
                            + ". Ці випадки не можуть відбутися одночасно, тому кількості сприятливих наслідків додаємо:";
                    StringBuilder f = new StringBuilder("m = ");
                    for (int i = 0; i < ks.size(); i++) {
                        if (i > 0) {
                            f.append(" + ");
                        }
                        f.append(Fmt.c(n, ks.get(i)));
                    }
                    f.append(" = ").append(favorable);
                    formula = f.toString();
                }
            }
            case COINS_ALL_SAME -> {
                text = "Сприятливі лише дві послідовності: усі монети гербом або всі цифрою.";
                formula = "m = 2";
            }
            default -> {
                text = "";
                formula = null;
            }
        }
        return ExplainStep.of("Сприятливі наслідки", text, formula);
    }

    private static String joinKs(List<Integer> ks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ks.size(); i++) {
            if (i > 0) {
                sb.append(i == ks.size() - 1 ? " або " : ", ");
            }
            sb.append(ks.get(i));
        }
        return sb.toString();
    }

    static String simplified(Fraction prob, BigInteger favorable, BigInteger total) {
        if (prob.numerator().equals(favorable) && prob.denominator().equals(total)) {
            return "";
        }
        return " = " + Fmt.frac(prob);
    }

    static ExplainStep plainWords(Fraction prob) {
        long per1000 = Math.round(prob.toDouble() * 1000);
        String text;
        if (prob.isZero()) {
            text = "Подія A неможлива: вона не відбудеться в жодному випробуванні.";
        } else if (prob.equals(Fraction.ONE)) {
            text = "Подія A вірогідна: вона відбувається в кожному випробуванні.";
        } else {
            text = "Якщо повторити випробування 1000 разів, подія A відбудеться приблизно " + per1000 + " "
                    + Fmt.plural(per1000, "раз", "рази", "разів") + " (≈ " + Fmt.percent(prob)
                    + "). Точна кількість щоразу буде трохи іншою — так поводяться випадкові події. Чим більше випробувань, тим ближче відносна частота до ймовірності.";
        }
        return ExplainStep.of("Простими словами", text);
    }

    private String eventText() {
        return switch (event) {
            case HEADS_EXACTLY -> n == 1 ? (value == 1 ? "«випав герб»" : "«випала цифра»")
                    : "«випало рівно " + Fmt.count(value, "герб", "герби", "гербів") + "»";
            case HEADS_AT_LEAST -> "«випало не менше " + value + " " + Fmt.plural(value, "герба", "гербів", "гербів") + "»";
            case HEADS_AT_MOST -> "«випало не більше " + value + " " + Fmt.plural(value, "герба", "гербів", "гербів") + "»";
            case COINS_ALL_SAME -> "«усі монети впали однаково»";
            case HEADS_AFTER_STREAK -> "«після серії з " + Fmt.count(value, "герба", "гербів", "гербів") + " поспіль знову випав герб»";
            default -> "";
        };
    }

    private Theory streakTheory() {
        String experimentText = "Кидаємо монету, доки не випаде " + Fmt.count(value, "герб", "герби", "гербів")
                + " поспіль, і записуємо результат наступного кидка";
        String eventText = eventText();
        List<ExplainStep> steps = new ArrayList<>();
        steps.add(ExplainStep.of("Випробування", experimentText + ". Подія A — " + eventText + "."));
        steps.add(ExplainStep.of("Чи «пам'ятає» монета?",
                "Кидки монети незалежні: результат наступного кидка не залежить від того, що випадало раніше. Тому після будь-якої серії ймовірність герба така сама, як і завжди:",
                "P(A) = p = " + Fmt.frac(p) + " " + Fmt.approx(p)));
        steps.add(ExplainStep.of("Помилка гравця",
                "Багато хто вважає, що після кількох гербів поспіль «настала черга» цифри. Це хибне уявлення називають помилкою гравця. "
                        + "Запустіть моделювання: відносна частота герба після серії все одно наближається до " + Fmt.decimal(p) + "."));
        steps.add(plainWords(p));
        return new Theory(experimentText, eventText, p.toString(), Fmt.frac(p), Fmt.decimal(p), Fmt.approx(p).substring(0, 1),
                p.toDouble(), null, null, "незалежність випробувань", steps, null, distribution());
    }

    private OutcomeTable outcomeList() {
        if (n > 4 || event == EventKind.HEADS_AFTER_STREAK) {
            return null;
        }
        List<String> items = new ArrayList<>();
        List<Boolean> marks = new ArrayList<>();
        int total = 1 << n;
        for (int mask = total - 1; mask >= 0; mask--) {
            StringBuilder sb = new StringBuilder();
            int heads = 0;
            for (int i = n - 1; i >= 0; i--) {
                boolean h = ((mask >> i) & 1) == 1;
                sb.append(h ? 'Г' : 'Ц');
                if (h) {
                    heads++;
                }
            }
            items.add(sb.toString());
            marks.add(inEvent[heads]);
        }
        String caption = fair ? "Усі " + total + " рівноможливих наслідків; сприятливі для A виділено"
                : "Усі можливі наслідки (через нечесну монету вони не рівноможливі); сприятливі для A виділено";
        return new OutcomeTable("list", caption, null, null, List.of(items), List.of(marks));
    }

    private Distribution distribution() {
        List<String> labels = new ArrayList<>();
        List<Double> probs = new ArrayList<>();
        List<String> fracs = new ArrayList<>();
        List<Boolean> marks = new ArrayList<>();
        if (event == EventKind.HEADS_AFTER_STREAK) {
            Fraction q = p.complement();
            labels.add("Ц");
            probs.add(q.toDouble());
            fracs.add(q.toString());
            marks.add(false);
            labels.add("Г");
            probs.add(p.toDouble());
            fracs.add(p.toString());
            marks.add(true);
            return new Distribution("наступний кидок після серії", 0, labels, probs, fracs, marks);
        }
        for (int k = 0; k <= n; k++) {
            Fraction f = pk(k);
            labels.add(String.valueOf(k));
            probs.add(f.toDouble());
            fracs.add(f.toString());
            marks.add(inEvent[k]);
        }
        return new Distribution("кількість гербів", 0, labels, probs, fracs, marks);
    }
}
