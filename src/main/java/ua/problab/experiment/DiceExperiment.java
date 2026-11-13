/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

import ua.problab.math.Fmt;
import ua.problab.math.Fraction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/** Кидання n гральних кубиків (n = 1…4). */
final class DiceExperiment implements Experiment {

    static final int MAX_DICE = 4;

    private final ExperimentConfig cfg;
    private final int n;
    private final EventKind event;
    private final int value;
    private final int statMin;
    private final int statMax;
    private final boolean[] inEvent;

    DiceExperiment(ExperimentConfig cfg) {
        int count = cfg.count() == null ? 2 : cfg.count();
        if (count < 1 || count > MAX_DICE) {
            throw new IllegalArgumentException("Кількість кубиків має бути від 1 до " + MAX_DICE);
        }
        this.n = count;
        this.event = cfg.event() == null ? EventKind.SUM_EQ : cfg.event();
        int v = cfg.value() == null ? defaultValue() : cfg.value();
        switch (event) {
            case SUM_EQ, SUM_AT_LEAST, SUM_AT_MOST -> {
                if (v < n || v > 6 * n) {
                    throw new IllegalArgumentException("Сума очок на " + Fmt.count(n, "кубику", "кубиках", "кубиках")
                            + " може бути від " + n + " до " + 6 * n);
                }
                statMin = n;
                statMax = 6 * n;
            }
            case SUM_EVEN -> {
                statMin = n;
                statMax = 6 * n;
            }
            case AT_LEAST_ONE_FACE -> {
                if (v < 1 || v > 6) {
                    throw new IllegalArgumentException("Кількість очок на грані – від 1 до 6");
                }
                statMin = 0;
                statMax = n;
            }
            case DICE_ALL_SAME -> {
                if (n < 2) {
                    throw new IllegalArgumentException("Подія «на всіх кубиках однаково» має сенс, якщо кубиків щонайменше два");
                }
                statMin = 1;
                statMax = Math.min(n, 6);
            }
            default -> throw new IllegalArgumentException("Ця подія не стосується кубиків");
        }
        this.value = v;
        this.cfg = new ExperimentConfig(ExperimentType.DICE, n, null, null, null, null, null, event, v);
        inEvent = new boolean[statMax - statMin + 1];
        for (int s = statMin; s <= statMax; s++) {
            inEvent[s - statMin] = switch (event) {
                case SUM_EQ -> s == value;
                case SUM_AT_LEAST -> s >= value;
                case SUM_AT_MOST -> s <= value;
                case SUM_EVEN -> s % 2 == 0;
                case AT_LEAST_ONE_FACE -> s >= 1;
                case DICE_ALL_SAME -> s == 1;
                default -> false;
            };
        }
    }

    private int defaultValue() {
        return switch (event) {
            case AT_LEAST_ONE_FACE -> 6;
            case SUM_EQ, SUM_AT_LEAST, SUM_AT_MOST -> Math.round(3.5f * n);
            default -> 0;
        };
    }

    @Override
    public ExperimentConfig config() {
        return cfg;
    }

    @Override
    public int statisticMin() {
        return statMin;
    }

    @Override
    public int statisticMax() {
        return statMax;
    }

    private int statistic(int[] faces) {
        switch (event) {
            case AT_LEAST_ONE_FACE: {
                int c = 0;
                for (int f : faces) {
                    if (f == value) {
                        c++;
                    }
                }
                return c;
            }
            case DICE_ALL_SAME: {
                boolean[] seen = new boolean[7];
                int distinct = 0;
                for (int f : faces) {
                    if (!seen[f]) {
                        seen[f] = true;
                        distinct++;
                    }
                }
                return distinct;
            }
            default: {
                int s = 0;
                for (int f : faces) {
                    s += f;
                }
                return s;
            }
        }
    }

    @Override
    public Trial sample(RandomGenerator rnd) {
        int[] faces = new int[n];
        for (int i = 0; i < n; i++) {
            faces[i] = 1 + rnd.nextInt(6);
        }
        int st = statistic(faces);
        return new Trial(inEvent[st - statMin], st, faces);
    }

    @Override
    public List<String> tokens(int[] raw) {
        List<String> t = new ArrayList<>(raw.length);
        for (int f : raw) {
            t.add(String.valueOf(f));
        }
        return t;
    }

    // ------------------------------------------------------------------ теорія

    @Override
    public Theory theory() {
        int total = 1;
        for (int i = 0; i < n; i++) {
            total *= 6;
        }
        long[] countByStat = new long[statMax - statMin + 1];
        long[] countBySum = new long[6 * n + 1];
        List<int[]> favorableList = new ArrayList<>();
        long favorable = 0;
        int[] faces = new int[n];
        for (int code = 0; code < total; code++) {
            int c = code;
            int sum = 0;
            for (int i = n - 1; i >= 0; i--) {
                faces[i] = 1 + c % 6;
                c /= 6;
                sum += faces[i];
            }
            int st = statistic(faces);
            countByStat[st - statMin]++;
            countBySum[sum]++;
            if (inEvent[st - statMin]) {
                favorable++;
                if (favorableList.size() < 40) {
                    favorableList.add(faces.clone());
                }
            }
        }
        Fraction prob = Fraction.of(favorable, total);
        BigInteger fav = BigInteger.valueOf(favorable);
        BigInteger tot = BigInteger.valueOf(total);

        String experimentText = n == 1 ? "Кидаємо гральний кубик" : "Кидаємо " + n + " гральні кубики";
        String eventText = eventText();
        List<ExplainStep> steps = new ArrayList<>();
        steps.add(ExplainStep.of("Випробування", experimentText + ". Подія A – " + eventText + "."));
        if (n == 1) {
            steps.add(ExplainStep.of("Усі рівноможливі наслідки",
                    "На верхній грані може випасти 1, 2, 3, 4, 5 або 6 очок. Кубик правильний, тому всі 6 наслідків рівноможливі.", "n = 6"));
        } else {
            steps.add(ExplainStep.of("Усі рівноможливі наслідки",
                    "Наслідок – це впорядкований набір чисел на кубиках, наприклад " + example()
                            + ". Уявіть, що кубики різного кольору: тоді " + example() + " і " + exampleReversed()
                            + " – різні наслідки. За правилом добутку кількість усіх наслідків:",
                    "n = 6^{" + n + "} = " + total));
        }
        steps.add(favorableStep(favorable, favorableList, countBySum));
        steps.add(ExplainStep.of("Ймовірність", "За класичним означенням ймовірності:",
                "P(A) = " + Fmt.frac("m", "n") + " = " + Fmt.frac(fav, tot) + CoinsExperiment.simplified(prob, fav, tot) + " " + Fmt.approx(prob)));
        if (event == EventKind.AT_LEAST_ONE_FACE && n >= 2) {
            Fraction q = Fraction.of(5, 6).pow(n);
            steps.add(ExplainStep.of("Інший спосіб – протилежна подія",
                    "Ймовірність того, що на одному кубику не випаде " + value + ", дорівнює 5/6. Кубики незалежні, тому ймовірність, що " + value
                            + " не випаде на жодному з " + n + " кубиків, дорівнює (5/6) у степені " + n + ". Отже:",
                    "P(A) = 1 - P(\\bar{A}) = 1 - \\left(" + Fmt.frac(5, 6) + "\\right)^{" + n + "} = 1 - " + Fmt.frac(q) + " = " + Fmt.frac(prob)));
        }
        steps.add(CoinsExperiment.plainWords(prob));
        if ((event == EventKind.SUM_EQ || event == EventKind.SUM_AT_LEAST || event == EventKind.SUM_AT_MOST) && n >= 2) {
            int mode = n;
            for (int s = n; s <= 6 * n; s++) {
                if (countBySum[s] > countBySum[mode]) {
                    mode = s;
                }
            }
            steps.add(ExplainStep.of("Зверніть увагу",
                    "Різні суми не є рівноможливими: суму " + mode + " можна отримати " + countBySum[mode] + " "
                            + Fmt.plural(countBySum[mode], "способом", "способами", "способами") + ", а суму " + (6 * n)
                            + " – лише одним. Тому не можна вважати, що всі " + (5 * n + 1) + " можливі суми мають однакову ймовірність."));
        }

        return new Theory(experimentText, eventText, prob.toString(), Fmt.frac(prob), Fmt.decimal(prob), Fmt.approx(prob).substring(0, 1),
                prob.toDouble(), String.valueOf(favorable), String.valueOf(total), "класичне означення ймовірності", steps,
                outcomeTable(), distribution(countByStat, total));
    }

    private String example() {
        return n == 2 ? "(2; 5)" : n == 3 ? "(2; 5; 1)" : "(2; 5; 1; 6)";
    }

    private String exampleReversed() {
        return n == 2 ? "(5; 2)" : n == 3 ? "(5; 2; 1)" : "(5; 2; 1; 6)";
    }

    private ExplainStep favorableStep(long favorable, List<int[]> favorableList, long[] countBySum) {
        String text;
        String formula = "m = " + favorable;
        switch (event) {
            case AT_LEAST_ONE_FACE -> {
                if (n == 1) {
                    text = "Події A сприяє один наслідок – випало " + value + ".";
                } else {
                    text = "Зручно перейти до протилежної події Ā «" + value + " не випало на жодному кубику». Тоді на кожному кубику 5 варіантів, і Ā сприяють 5^"
                            + n + " наслідків. Решта наслідків сприяють події A:";
                    formula = "m = 6^{" + n + "} - 5^{" + n + "} = " + pow(6, n) + " - " + pow(5, n) + " = " + favorable;
                }
            }
            case DICE_ALL_SAME -> {
                text = "Сприятливі наслідки: " + listOutcomes(favorableList, favorable) + ".";
            }
            case SUM_AT_LEAST, SUM_AT_MOST -> {
                List<Integer> sums = new ArrayList<>();
                for (int s = n; s <= 6 * n; s++) {
                    if (inEvent[s - statMin] && countBySum[s] > 0) {
                        sums.add(s);
                    }
                }
                if (sums.size() <= 6) {
                    StringBuilder f = new StringBuilder("m = ");
                    for (int i = 0; i < sums.size(); i++) {
                        if (i > 0) {
                            f.append(" + ");
                        }
                        f.append(countBySum[sums.get(i)]);
                    }
                    f.append(" = ").append(favorable);
                    formula = f.toString();
                    text = "Підходять суми " + joinInts(sums) + ". Додаємо кількості наслідків для кожної з цих сум"
                            + (n == 2 ? " (їх зручно порахувати за таблицею 6 × 6):" : ":");
                } else {
                    text = "Підходять суми від " + sums.get(0) + " до " + sums.get(sums.size() - 1)
                            + ". Підрахуємо всі набори з такими сумами" + (n == 2 ? " (за таблицею 6 × 6):" : ":");
                }
            }
            case SUM_EVEN -> {
                text = n == 1 ? "Події A сприяють наслідки 2, 4, 6."
                        : "Сума парна, якщо непарних чисел серед випалих парна кількість. Таких наслідків рівно половина:";
            }
            default -> {
                if (favorable == 0) {
                    text = "Жоден наслідок не дає такої суми – подія неможлива.";
                } else if (favorable <= 12) {
                    text = "Події A сприяють наслідки: " + listOutcomes(favorableList, favorable) + ".";
                } else {
                    text = "Перелічимо всі набори з потрібною сумою" + (n == 2 ? " (за таблицею 6 × 6)" : "") + ". Їх кількість:";
                }
            }
        }
        return ExplainStep.of("Сприятливі наслідки", text, formula);
    }

    private static long pow(int base, int exp) {
        long r = 1;
        for (int i = 0; i < exp; i++) {
            r *= base;
        }
        return r;
    }

    private String listOutcomes(List<int[]> list, long favorable) {
        StringBuilder sb = new StringBuilder();
        int limit = (int) Math.min(list.size(), 12);
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            int[] f = list.get(i);
            if (n == 1) {
                sb.append(f[0]);
            } else {
                sb.append('(');
                for (int j = 0; j < f.length; j++) {
                    if (j > 0) {
                        sb.append("; ");
                    }
                    sb.append(f[j]);
                }
                sb.append(')');
            }
        }
        if (favorable > limit) {
            sb.append(", … (усього ").append(favorable).append(')');
        }
        return sb.toString();
    }

    private static String joinInts(List<Integer> xs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < xs.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(xs.get(i));
        }
        return sb.toString();
    }

    private String eventText() {
        String one = n == 1 ? "випало" : "сума очок";
        return switch (event) {
            case SUM_EQ -> n == 1 ? "«випало " + Fmt.count(value, "очко", "очки", "очок") + "»" : "«сума очок дорівнює " + value + "»";
            case SUM_AT_LEAST -> n == 1 ? "«випало не менше " + value + " " + Fmt.plural(value, "очка", "очок", "очок") + "»"
                    : "«сума очок не менша від " + value + "»";
            case SUM_AT_MOST -> n == 1 ? "«випало не більше " + value + " " + Fmt.plural(value, "очка", "очок", "очок") + "»"
                    : "«сума очок не більша за " + value + "»";
            case SUM_EVEN -> n == 1 ? "«випало парне число очок»" : "«" + one + " – парне число»";
            case AT_LEAST_ONE_FACE -> n == 1 ? "«випало " + Fmt.count(value, "очко", "очки", "очок") + "»"
                    : "«хоча б на одному кубику випало " + Fmt.count(value, "очко", "очки", "очок") + "»";
            case DICE_ALL_SAME -> "«на всіх кубиках однакова кількість очок»";
            default -> "";
        };
    }

    private OutcomeTable outcomeTable() {
        if (n == 1) {
            List<String> items = new ArrayList<>();
            List<Boolean> marks = new ArrayList<>();
            for (int f = 1; f <= 6; f++) {
                items.add(String.valueOf(f));
                marks.add(inEvent[statistic(new int[]{f}) - statMin]);
            }
            return new OutcomeTable("list", "Усі 6 рівноможливих наслідків; сприятливі для A виділено", null, null, List.of(items), List.of(marks));
        }
        if (n != 2) {
            return null;
        }
        boolean sumCells = event == EventKind.SUM_EQ || event == EventKind.SUM_AT_LEAST || event == EventKind.SUM_AT_MOST || event == EventKind.SUM_EVEN;
        List<String> labels = List.of("1", "2", "3", "4", "5", "6");
        List<List<String>> cells = new ArrayList<>();
        List<List<Boolean>> marks = new ArrayList<>();
        for (int a = 1; a <= 6; a++) {
            List<String> row = new ArrayList<>();
            List<Boolean> mrow = new ArrayList<>();
            for (int b = 1; b <= 6; b++) {
                row.add(sumCells ? String.valueOf(a + b) : a + ";" + b);
                mrow.add(inEvent[statistic(new int[]{a, b}) - statMin]);
            }
            cells.add(row);
            marks.add(mrow);
        }
        String caption = "Усі 36 наслідків: рядок – перший кубик, стовпець – другий"
                + (sumCells ? "; у клітинках – сума очок" : "") + ". Сприятливі для A виділено";
        return new OutcomeTable("grid", caption, labels, labels, cells, marks);
    }

    private Distribution distribution(long[] countByStat, int total) {
        List<String> labels = new ArrayList<>();
        List<Double> probs = new ArrayList<>();
        List<String> fracs = new ArrayList<>();
        List<Boolean> marks = new ArrayList<>();
        for (int s = statMin; s <= statMax; s++) {
            Fraction f = Fraction.of(countByStat[s - statMin], total);
            labels.add(String.valueOf(s));
            probs.add(f.toDouble());
            fracs.add(f.toString());
            marks.add(inEvent[s - statMin]);
        }
        String name = switch (event) {
            case AT_LEAST_ONE_FACE -> "скільки разів випало " + value;
            case DICE_ALL_SAME -> "кількість різних чисел";
            default -> n == 1 ? "кількість очок" : "сума очок";
        };
        return new Distribution(name, statMin, labels, probs, fracs, marks);
    }
}
