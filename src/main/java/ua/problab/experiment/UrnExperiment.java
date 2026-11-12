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

/** Виймання k куль з урни, де R червоних і B синіх куль. */
final class UrnExperiment implements Experiment {

    static final int MAX_BALLS = 30;
    static final int MAX_DRAW = 6;

    private final ExperimentConfig cfg;
    private final int red;
    private final int blue;
    private final int total;
    private final int k;
    private final boolean replacement;
    private final EventKind event;
    private final int value;
    private final boolean[] inEvent;

    UrnExperiment(ExperimentConfig cfg) {
        this.red = cfg.red() == null ? 5 : cfg.red();
        this.blue = cfg.blue() == null ? 3 : cfg.blue();
        if (red < 0 || blue < 0) {
            throw new IllegalArgumentException("Кількість куль не може бути від'ємною");
        }
        this.total = red + blue;
        if (total < 1 || total > MAX_BALLS) {
            throw new IllegalArgumentException("Усього куль в урні має бути від 1 до " + MAX_BALLS);
        }
        this.replacement = Boolean.TRUE.equals(cfg.replacement());
        this.k = cfg.draw() == null ? 1 : cfg.draw();
        int maxDraw = replacement ? MAX_DRAW : Math.min(MAX_DRAW, total);
        if (k < 1 || k > maxDraw) {
            throw new IllegalArgumentException(replacement
                    ? "Кількість виймань має бути від 1 до " + MAX_DRAW
                    : "Без повернення можна вийняти від 1 до " + maxDraw + " " + Fmt.plural(maxDraw, "кулі", "куль", "куль"));
        }
        this.event = cfg.event() == null ? EventKind.RED_EXACTLY : cfg.event();
        int v = cfg.value() == null ? (event == EventKind.URN_ALL_SAME ? 0 : Math.min(1, k)) : cfg.value();
        switch (event) {
            case RED_EXACTLY, RED_AT_LEAST -> {
                if (v < 0 || v > k) {
                    throw new IllegalArgumentException("Кількість червоних куль у події має бути від 0 до " + k);
                }
            }
            case URN_ALL_SAME -> {
                if (k < 2) {
                    throw new IllegalArgumentException("Подія «усі кулі одного кольору» має сенс, якщо виймають щонайменше дві кулі");
                }
            }
            default -> throw new IllegalArgumentException("Ця подія не стосується урни");
        }
        this.value = v;
        this.cfg = new ExperimentConfig(ExperimentType.URN, null, null, red, blue, k, replacement, event, v);
        inEvent = new boolean[k + 1];
        for (int j = 0; j <= k; j++) {
            inEvent[j] = switch (event) {
                case RED_EXACTLY -> j == value;
                case RED_AT_LEAST -> j >= value;
                case URN_ALL_SAME -> j == 0 || j == k;
                default -> false;
            };
        }
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
        return k;
    }

    @Override
    public Trial sample(RandomGenerator rnd) {
        int[] raw = new int[k];
        int reds = 0;
        if (replacement) {
            for (int i = 0; i < k; i++) {
                raw[i] = rnd.nextInt(total) < red ? 1 : 0;
                reds += raw[i];
            }
        } else {
            int redLeft = red;
            int left = total;
            for (int i = 0; i < k; i++) {
                boolean isRed = rnd.nextInt(left) < redLeft;
                raw[i] = isRed ? 1 : 0;
                if (isRed) {
                    redLeft--;
                    reds++;
                }
                left--;
            }
        }
        return new Trial(inEvent[reds], reds, raw);
    }

    @Override
    public List<String> tokens(int[] raw) {
        List<String> t = new ArrayList<>(raw.length);
        for (int r : raw) {
            t.add(r == 1 ? "Ч" : "С");
        }
        return t;
    }

    // ------------------------------------------------------------------ теорія

    /** Кількість сприятливих наслідків для «рівно j червоних». */
    private BigInteger ways(int j) {
        if (replacement) {
            return Combinatorics.binomial(k, j)
                    .multiply(Combinatorics.power(red, j))
                    .multiply(Combinatorics.power(blue, k - j));
        }
        return Combinatorics.binomial(red, j).multiply(Combinatorics.binomial(blue, k - j));
    }

    private BigInteger allWays() {
        return replacement ? Combinatorics.power(total, k) : Combinatorics.binomial(total, k);
    }

    @Override
    public Theory theory() {
        BigInteger all = allWays();
        BigInteger fav = BigInteger.ZERO;
        List<Integer> js = new ArrayList<>();
        for (int j = 0; j <= k; j++) {
            if (inEvent[j]) {
                fav = fav.add(ways(j));
                js.add(j);
            }
        }
        Fraction prob = Fraction.of(fav, all);
        String experimentText = "В урні " + Fmt.count(red, "червона куля", "червоні кулі", "червоних куль") + " і "
                + Fmt.count(blue, "синя", "сині", "синіх") + " (усього " + total + "). Навмання виймають "
                + (k == 1 ? "одну кулю" : Fmt.count(k, "кулю", "кулі", "куль") + (replacement ? " по одній, щоразу повертаючи кулю назад" : " без повернення"));
        String eventText = eventText();
        List<ExplainStep> steps = new ArrayList<>();
        steps.add(ExplainStep.of("Випробування", experimentText + ". Подія A — " + eventText + "."));

        String method;
        if (k == 1) {
            method = "класичне означення ймовірності";
            steps.add(ExplainStep.of("Усі рівноможливі наслідки",
                    "Кулі однакові на дотик, тому кожна з " + total + " куль може бути вийнята з однаковою ймовірністю.", "n = " + total));
            int j = js.isEmpty() ? 0 : js.get(0);
            steps.add(ExplainStep.of("Сприятливі наслідки",
                    js.size() == 2 ? "Будь-яка куля підходить." : (j == 1 ? "Сприятливі — червоні кулі:" : "Сприятливі — сині кулі:"),
                    "m = " + fav));
        } else if (!replacement) {
            method = "класичне означення ймовірності та комбінації";
            steps.add(ExplainStep.of("Усі рівноможливі наслідки",
                    "Наслідок — це набір з " + Fmt.count(k, "кулі", "куль", "куль") + " (порядок не важливий). Кількість способів вибрати "
                            + k + " з " + total + " — число комбінацій:",
                    "n = " + Fmt.c(total, k) + " = " + all));
            steps.add(ExplainStep.of("Сприятливі наслідки", favorableTextWithout(js), favorableFormula(js, fav)));
        } else {
            method = "класичне означення ймовірності (вибір з поверненням)";
            steps.add(ExplainStep.of("Усі рівноможливі наслідки",
                    "Пронумеруємо кулі. Після кожного виймання кулю повертають, тому склад урни не змінюється. Наслідок — упорядкований запис номерів " + k
                            + " вийнятих куль. За правилом добутку:",
                    "n = " + total + "^{" + k + "} = " + all));
            steps.add(ExplainStep.of("Сприятливі наслідки",
                    "Для «рівно j червоних» треба вибрати, у яких j вийманнях з'явиться червона куля (C з " + k
                            + " по j способів), і яка саме куля буде щоразу: червона — " + red + " варіантів, синя — " + blue + ".",
                    favorableFormula(js, fav)));
        }
        steps.add(ExplainStep.of("Ймовірність", "За класичним означенням ймовірності:",
                "P(A) = " + Fmt.frac("m", "n") + " = " + Fmt.frac(fav, all) + CoinsExperiment.simplified(prob, fav, all) + " " + Fmt.approx(prob)));
        if (replacement && k > 1) {
            steps.add(ExplainStep.of("Для профільного рівня",
                    "Виймання з поверненням незалежні, і ймовірність червоної кулі щоразу p = " + red + "/" + total
                            + ". Тому можна скористатися формулою Бернуллі — результат той самий:",
                    "P_{" + k + "}(j) = C_{" + k + "}^{j} \\cdot p^{j} \\cdot (1 - p)^{" + k + " - j}"));
        }
        if (!replacement && k > 1) {
            steps.add(ExplainStep.of("Порівняйте",
                    "Без повернення склад урни змінюється після кожного виймання, тому виймання залежні. Увімкніть «з поверненням» і порівняйте ймовірності."));
        }
        steps.add(CoinsExperiment.plainWords(prob));

        return new Theory(experimentText, eventText, prob.toString(), Fmt.frac(prob), Fmt.decimal(prob), Fmt.approx(prob).substring(0, 1),
                prob.toDouble(), fav.toString(), all.toString(), method, steps, null, distribution(all));
    }

    private String favorableTextWithout(List<Integer> js) {
        if (event == EventKind.URN_ALL_SAME) {
            return "Сприятливі набори: усі " + k + " кулі червоні (C з " + red + " по " + k + " способів) або всі сині (C з " + blue + " по " + k
                    + " способів). Ці випадки несумісні, тому кількості додаємо:";
        }
        if (js.size() == 1) {
            int j = js.get(0);
            return "Потрібно вибрати " + j + " " + Fmt.plural(j, "червону кулю", "червоні кулі", "червоних куль") + " з " + red + " і "
                    + (k - j) + " " + Fmt.plural(k - j, "синю", "сині", "синіх") + " з " + blue + ". За правилом добутку:";
        }
        return "Подія A означає, що червоних куль " + joinInts(js) + ". Для кожного випадку застосовуємо правило добутку, а результати додаємо:";
    }

    private String favorableFormula(List<Integer> js, BigInteger fav) {
        StringBuilder f = new StringBuilder("m = ");
        int shown = 0;
        for (int j : js) {
            if (shown > 0) {
                f.append(" + ");
            }
            if (replacement) {
                f.append(Fmt.c(k, j)).append(" \\cdot ").append(red).append("^{").append(j).append("} \\cdot ").append(blue).append("^{").append(k - j).append("}");
            } else {
                f.append(Fmt.c(red, j)).append(" \\cdot ").append(Fmt.c(blue, k - j));
            }
            shown++;
            if (shown == 3 && js.size() > 4) {
                f.append(" + …");
                break;
            }
        }
        f.append(" = ").append(fav);
        return f.toString();
    }

    private static String joinInts(List<Integer> xs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < xs.size(); i++) {
            if (i > 0) {
                sb.append(i == xs.size() - 1 ? " або " : ", ");
            }
            sb.append(xs.get(i));
        }
        return sb.toString();
    }

    private String eventText() {
        if (k == 1) {
            if (event == EventKind.RED_AT_LEAST && value == 0) {
                return "«вийняли кулю будь-якого кольору»";
            }
            return value == 1 ? "«вийняли червону кулю»" : "«вийняли синю кулю»";
        }
        return switch (event) {
            case RED_EXACTLY -> value == 0 ? "«серед вийнятих немає червоних куль»"
                    : "«серед вийнятих рівно " + Fmt.count(value, "червона куля", "червоні кулі", "червоних куль") + "»";
            case RED_AT_LEAST -> "«серед вийнятих не менше " + value + " " + Fmt.plural(value, "червоної кулі", "червоних куль", "червоних куль") + "»";
            case URN_ALL_SAME -> "«усі вийняті кулі одного кольору»";
            default -> "";
        };
    }

    private Distribution distribution(BigInteger all) {
        List<String> labels = new ArrayList<>();
        List<Double> probs = new ArrayList<>();
        List<String> fracs = new ArrayList<>();
        List<Boolean> marks = new ArrayList<>();
        for (int j = 0; j <= k; j++) {
            Fraction f = Fraction.of(ways(j), all);
            labels.add(String.valueOf(j));
            probs.add(f.toDouble());
            fracs.add(f.toString());
            marks.add(inEvent[j]);
        }
        return new Distribution("кількість червоних куль", 0, labels, probs, fracs, marks);
    }
}
