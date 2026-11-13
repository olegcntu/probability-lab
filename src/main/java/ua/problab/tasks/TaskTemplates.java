/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.tasks;

import ua.problab.experiment.EventKind;
import ua.problab.experiment.ExperimentConfig;
import ua.problab.math.Combinatorics;
import ua.problab.math.Fmt;
import ua.problab.math.Fraction;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.random.RandomGenerator;

/**
 * Параметризовані шаблони задач на класичне означення ймовірності.
 * Кожен шаблон за випадковими числами створює нову задачу разом із відповіддю,
 * підказкою, розв'язанням і параметрами для перевірки моделюванням.
 * Рівні: 1 – базовий (один предмет, одна дія), 2 – середній (два об'єкти, перелік наслідків,
 * протилежна подія), 3 – достатній і високий (комбінації, вибір з поверненням і без).
 */
public final class TaskTemplates {

    public record Template(String id, Topic topic, int level, Function<RandomGenerator, GeneratedTask> generator) {
    }

    public static final List<Template> ALL = List.of(
            new Template("die-one", Topic.DICE, 1, TaskTemplates::dieOne),
            new Template("urn-one", Topic.URN, 1, TaskTemplates::urnOne),
            new Template("coins-two", Topic.COINS, 1, TaskTemplates::coinsTwo),
            new Template("numbers-div", Topic.NUMBERS, 1, TaskTemplates::numbersDivisible),
            new Template("exam-tickets", Topic.NUMBERS, 1, TaskTemplates::examTickets),
            new Template("phone-digit", Topic.NUMBERS, 1, TaskTemplates::phoneDigit),

            new Template("two-dice-sum", Topic.DICE, 2, TaskTemplates::twoDiceSum),
            new Template("three-coins", Topic.COINS, 2, TaskTemplates::threeCoins),
            new Template("two-dice-same", Topic.DICE, 2, TaskTemplates::twoDiceSame),
            new Template("class-duty", Topic.URN, 2, TaskTemplates::classDuty),
            new Template("coins-at-least-one", Topic.COINS, 2, TaskTemplates::coinsAtLeastOne),

            new Template("urn-hyper", Topic.URN, 3, TaskTemplates::urnHyper),
            new Template("die-six-at-least-once", Topic.DICE, 3, TaskTemplates::dieSixAtLeastOnce),
            new Template("urn-replacement", Topic.URN, 3, TaskTemplates::urnReplacement),
            new Template("coins-many", Topic.COINS, 3, TaskTemplates::coinsMany),
            new Template("phone-two-digits", Topic.NUMBERS, 3, TaskTemplates::phoneTwoDigits),
            new Template("olympiad-team", Topic.URN, 3, TaskTemplates::olympiadTeam),
            new Template("three-dice-sum", Topic.DICE, 3, TaskTemplates::threeDiceSum));

    private TaskTemplates() {
    }

    // ------------------------------------------------------------------ рівень 1

    static GeneratedTask dieOne(RandomGenerator r) {
        int kind = r.nextInt(4);
        String text;
        int m;
        String favorable;
        ExperimentConfig lab;
        switch (kind) {
            case 0 -> {
                int v = between(r, 1, 6);
                text = "Кидають гральний кубик. Знайдіть ймовірність того, що випаде " + Fmt.count(v, "очко", "очки", "очок") + ".";
                m = 1;
                favorable = "Сприятливий один наслідок – випало " + v + ": $m = 1$.";
                lab = ExperimentConfig.dice(1, EventKind.SUM_EQ, v);
            }
            case 1 -> {
                int a = between(r, 3, 5);
                text = "Кидають гральний кубик. Знайдіть ймовірність того, що випаде не менше " + a + " " + Fmt.plural(a, "очка", "очок", "очок") + ".";
                m = 7 - a;
                favorable = "Сприятливі наслідки: " + range(a, 6) + " – $m = " + m + "$.";
                lab = ExperimentConfig.dice(1, EventKind.SUM_AT_LEAST, a);
            }
            case 2 -> {
                int a = between(r, 2, 4);
                text = "Кидають гральний кубик. Знайдіть ймовірність того, що випаде не більше " + a + " " + Fmt.plural(a, "очка", "очок", "очок") + ".";
                m = a;
                favorable = "Сприятливі наслідки: " + range(1, a) + " – $m = " + m + "$.";
                lab = ExperimentConfig.dice(1, EventKind.SUM_AT_MOST, a);
            }
            default -> {
                text = "Кидають гральний кубик. Знайдіть ймовірність того, що випаде парне число очок.";
                m = 3;
                favorable = "Сприятливі наслідки: 2, 4, 6 – $m = 3$.";
                lab = ExperimentConfig.dice(1, EventKind.SUM_EVEN, null);
            }
        }
        return task("die-one", Topic.DICE, 1, text, m, 6,
                "Скільки всього рівноможливих наслідків при киданні кубика? Скільки з них сприяють події?",
                List.of("Усього рівноможливих наслідків: $n = 6$.", favorable, probFormula(m, 6)), lab);
    }

    static GeneratedTask urnOne(RandomGenerator r) {
        int red = between(r, 2, 9);
        int blue = between(r, 2, 9);
        boolean askRed = r.nextBoolean();
        int total = red + blue;
        int m = askRed ? red : blue;
        String text = "У коробці " + balls(red, blue) + ", однакових на дотик. Навмання беруть одну кулю. Яка ймовірність того, що вона "
                + (askRed ? "червона" : "синя") + "?";
        return task("urn-one", Topic.URN, 1, text, m, total,
                "Кожна куля може бути вийнята з однаковою ймовірністю. Скільки всього куль і скільки з них потрібного кольору?",
                List.of("Усього рівноможливих наслідків (куль): $n = " + red + " + " + blue + " = " + total + "$.",
                        "Сприятливі – " + (askRed ? "червоні" : "сині") + " кулі: $m = " + m + "$.",
                        probFormula(m, total)),
                ExperimentConfig.urn(red, blue, 1, false, EventKind.RED_EXACTLY, askRed ? 1 : 0));
    }

    static GeneratedTask coinsTwo(RandomGenerator r) {
        int k = r.nextInt(3);
        String what = switch (k) {
            case 0 -> "герб не випаде жодного разу";
            case 1 -> "герб випаде рівно один раз";
            default -> "герб випаде обидва рази";
        };
        String fav = switch (k) {
            case 0 -> "ЦЦ – $m = 1$";
            case 1 -> "ГЦ, ЦГ – $m = 2$";
            default -> "ГГ – $m = 1$";
        };
        int m = k == 1 ? 2 : 1;
        return task("coins-two", Topic.COINS, 1, "Монету кидають двічі. Яка ймовірність того, що " + what + "?", m, 4,
                "Випишіть усі можливі результати двох кидків: ГГ, ГЦ, … Чи всі вони рівноможливі?",
                List.of("Усі рівноможливі наслідки: ГГ, ГЦ, ЦГ, ЦЦ – $n = 4$.", "Сприятливі наслідки: " + fav + ".", probFormula(m, 4)),
                ExperimentConfig.coins(2, EventKind.HEADS_EXACTLY, k));
    }

    static GeneratedTask numbersDivisible(RandomGenerator r) {
        int[] ns = {10, 12, 15, 20, 24, 25, 30, 36, 40, 50, 60, 100};
        int n = ns[r.nextInt(ns.length)];
        int d = between(r, 2, 9);
        int m = n / d;
        String text = "Із натуральних чисел від 1 до " + n + " навмання вибирають одне. Яка ймовірність того, що воно ділиться на " + d + "?";
        return task("numbers-div", Topic.NUMBERS, 1, text, m, n,
                "Скільки чисел від 1 до " + n + " діляться на " + d + "? Це числа " + d + ", " + 2 * d + ", " + 3 * d + ", …",
                List.of("Усього рівноможливих наслідків: $n = " + n + "$.",
                        "Числа, кратні " + d + ": " + d + ", " + 2 * d + ", …, " + (m * d) + " – їх $m = " + m + "$ (ціла частина від ділення " + n + " на " + d + ").",
                        probFormula(m, n)),
                null);
    }

    static GeneratedTask examTickets(RandomGenerator r) {
        int n = 5 * between(r, 4, 8);
        int learned = between(r, n / 2, n - 2);
        String text = "До заліку підготовлено " + n + " " + Fmt.plural(n, "білет", "білети", "білетів") + ". Учень вивчив " + learned
                + " з них. Яка ймовірність того, що йому дістанеться вивчений білет?";
        return task("exam-tickets", Topic.NUMBERS, 1, text, learned, n,
                "Кожен білет може дістатися з однаковою ймовірністю.",
                List.of("Усього рівноможливих наслідків: $n = " + n + "$.", "Сприятливі – вивчені білети: $m = " + learned + "$.", probFormula(learned, n)),
                null);
    }

    static GeneratedTask phoneDigit(RandomGenerator r) {
        boolean parity = r.nextBoolean();
        if (!parity) {
            return task("phone-digit", Topic.NUMBERS, 1,
                    "Абонент забув останню цифру номера телефону і набирає її навмання. Яка ймовірність того, що він набере правильний номер з першої спроби?",
                    1, 10, "Скільки існує різних цифр?",
                    List.of("Останньою може бути будь-яка з 10 цифр: $n = 10$.", "Правильна лише одна: $m = 1$.", probFormula(1, 10)), null);
        }
        boolean even = r.nextBoolean();
        return task("phone-digit", Topic.NUMBERS, 1,
                "Абонент забув останню цифру номера телефону, але пам'ятає, що вона " + (even ? "парна" : "непарна")
                        + ". Він набирає її навмання. Яка ймовірність того, що номер буде набрано правильно з першої спроби?",
                1, 5, "Скільки існує " + (even ? "парних" : "непарних") + " цифр?",
                List.of((even ? "Парні цифри: 0, 2, 4, 6, 8" : "Непарні цифри: 1, 3, 5, 7, 9") + " – $n = 5$.", "Правильна лише одна: $m = 1$.",
                        probFormula(1, 5)), null);
    }

    // ------------------------------------------------------------------ рівень 2

    static GeneratedTask twoDiceSum(RandomGenerator r) {
        int kind = r.nextInt(3);
        int s;
        EventKind ev;
        String cond;
        if (kind == 0) {
            s = between(r, 3, 11);
            ev = EventKind.SUM_EQ;
            cond = "дорівнює " + s;
        } else if (kind == 1) {
            s = between(r, 8, 11);
            ev = EventKind.SUM_AT_LEAST;
            cond = "не менша від " + s;
        } else {
            s = between(r, 3, 6);
            ev = EventKind.SUM_AT_MOST;
            cond = "не більша за " + s;
        }
        List<String> pairs = new ArrayList<>();
        for (int a = 1; a <= 6; a++) {
            for (int b = 1; b <= 6; b++) {
                int sum = a + b;
                boolean ok = ev == EventKind.SUM_EQ ? sum == s : ev == EventKind.SUM_AT_LEAST ? sum >= s : sum <= s;
                if (ok) {
                    pairs.add("(" + a + "; " + b + ")");
                }
            }
        }
        int m = pairs.size();
        String listed = m <= 10 ? String.join(", ", pairs) : String.join(", ", pairs.subList(0, 6)) + ", …";
        return task("two-dice-sum", Topic.DICE, 2, "Кидають два гральні кубики. Знайдіть ймовірність того, що сума очок " + cond + ".", m, 36,
                "Складіть таблицю 6 × 6 усіх наслідків (перший кубик – рядок, другий – стовпець) і позначте клітинки з потрібною сумою.",
                List.of("Наслідок – упорядкована пара чисел; за правилом добутку $n = 6 \\cdot 6 = 36$.",
                        "Сприятливі наслідки: " + listed + " – $m = " + m + "$.", probFormula(m, 36)),
                ExperimentConfig.dice(2, ev, s));
    }

    static GeneratedTask threeCoins(RandomGenerator r) {
        boolean atLeast = r.nextBoolean();
        int k = atLeast ? between(r, 1, 2) : between(r, 0, 3);
        int m;
        String cond;
        if (atLeast) {
            m = 0;
            for (int i = k; i <= 3; i++) {
                m += Combinatorics.binomial(3, i).intValue();
            }
            cond = "не менше " + k + " " + Fmt.plural(k, "герба", "гербів", "гербів");
        } else {
            m = Combinatorics.binomial(3, k).intValue();
            cond = "рівно " + Fmt.count(k, "герб", "герби", "гербів");
        }
        String question = (!atLeast && k == 0) ? "не випаде жодного герба" : "випаде " + cond;
        return task("three-coins", Topic.COINS, 2, "Кидають три монети. Яка ймовірність того, що " + question + "?", m, 8,
                "Випишіть усі послідовності з трьох букв Г і Ц: ГГГ, ГГЦ, … Скільки їх?",
                List.of("Усі рівноможливі наслідки: ГГГ, ГГЦ, ГЦГ, ЦГГ, ГЦЦ, ЦГЦ, ЦЦГ, ЦЦЦ – $n = 2^{3} = 8$.",
                        "Сприятливих наслідків: $m = " + m + "$.", probFormula(m, 8)),
                ExperimentConfig.coins(3, atLeast ? EventKind.HEADS_AT_LEAST : EventKind.HEADS_EXACTLY, k));
    }

    static GeneratedTask twoDiceSame(RandomGenerator r) {
        boolean same = r.nextInt(3) != 0;
        if (same) {
            return task("two-dice-same", Topic.DICE, 2,
                    "Кидають два гральні кубики. Яка ймовірність того, що на обох кубиках випаде однакова кількість очок?", 6, 36,
                    "Скільки є пар виду (1; 1), (2; 2), …?",
                    List.of("$n = 6 \\cdot 6 = 36$.", "Сприятливі: (1; 1), (2; 2), (3; 3), (4; 4), (5; 5), (6; 6) – $m = 6$.", probFormula(6, 36)),
                    ExperimentConfig.dice(2, EventKind.DICE_ALL_SAME, null));
        }
        return task("two-dice-same", Topic.DICE, 2,
                "Кидають два гральні кубики. Яка ймовірність того, що на кубиках випаде різна кількість очок?", 30, 36,
                "Скористайтеся протилежною подією: «на кубиках однакова кількість очок».",
                List.of("$n = 36$.", "Протилежній події «однакова кількість очок» сприяють 6 наслідків, тому $m = 36 - 6 = 30$.", probFormula(30, 36)),
                null);
    }

    static GeneratedTask classDuty(RandomGenerator r) {
        int boys = between(r, 8, 14);
        int girls = between(r, 8, 14);
        boolean askGirls = r.nextBoolean();
        int total = boys + girls;
        int part = askGirls ? girls : boys;
        BigInteger n = Combinatorics.binomial(total, 2);
        BigInteger m = Combinatorics.binomial(part, 2);
        String who = askGirls ? "дівчатами" : "хлопцями";
        String text = "У класі " + boys + " " + Fmt.plural(boys, "хлопець", "хлопці", "хлопців") + " і " + girls + " "
                + Fmt.plural(girls, "дівчина", "дівчини", "дівчат") + ". Для чергування навмання обирають двох учнів. Яка ймовірність того, що обидва чергові будуть " + who + "?";
        ExperimentConfig lab = askGirls
                ? ExperimentConfig.urn(girls, boys, 2, false, EventKind.RED_EXACTLY, 2)
                : ExperimentConfig.urn(boys, girls, 2, false, EventKind.RED_EXACTLY, 2);
        return task("class-duty", Topic.URN, 2, text, m, n,
                "Порядок вибору не важливий, тому кількість наслідків – це число комбінацій з " + total + " по 2.",
                List.of("Вибрати двох учнів з " + total + ": $n = " + Fmt.c(total, 2) + " = " + n + "$.",
                        "Вибрати двох " + (askGirls ? "дівчат з " : "хлопців з ") + part + ": $m = " + Fmt.c(part, 2) + " = " + m + "$.",
                        probFormula(m, n)),
                lab);
    }

    static GeneratedTask coinsAtLeastOne(RandomGenerator r) {
        int n = between(r, 3, 5);
        int all = 1 << n;
        return task("coins-at-least-one", Topic.COINS, 2,
                "Монету кидають " + n + " " + Fmt.plural(n, "раз", "рази", "разів") + ". Яка ймовірність того, що герб випаде хоча б один раз?",
                all - 1, all,
                "Розгляньте протилежну подію: «герб не випав жодного разу».",
                List.of("Усього наслідків: $n = 2^{" + n + "} = " + all + "$.",
                        "Протилежній події Ā «жодного герба» сприяє лише один наслідок (усі цифри), тому $m = " + all + " - 1 = " + (all - 1) + "$.",
                        probFormula(all - 1, all)),
                ExperimentConfig.coins(n, EventKind.HEADS_AT_LEAST, 1));
    }

    // ------------------------------------------------------------------ рівень 3

    static GeneratedTask urnHyper(RandomGenerator r) {
        int red = between(r, 4, 9);
        int blue = between(r, 3, 8);
        int k = between(r, 2, 4);
        int j = between(r, 1, k - 1);
        BigInteger n = Combinatorics.binomial(red + blue, k);
        BigInteger m = Combinatorics.binomial(red, j).multiply(Combinatorics.binomial(blue, k - j));
        String text = "В урні " + balls(red, blue) + ". Навмання виймають " + Fmt.count(k, "кулю", "кулі", "куль")
                + ". Яка ймовірність того, що серед них рівно " + j + " " + Fmt.plural(j, "червона", "червоні", "червоних") + "?";
        return task("urn-hyper", Topic.URN, 3, text, m, n,
                "Кількість усіх наслідків – число комбінацій; для сприятливих скористайтеся правилом добутку.",
                List.of("Вибрати " + k + " " + Fmt.plural(k, "кулю", "кулі", "куль") + " з " + (red + blue) + ": $n = " + Fmt.c(red + blue, k) + " = " + n + "$.",
                        "Вибрати " + j + " " + Fmt.plural(j, "червону", "червоні", "червоних") + " з " + red + " і " + (k - j) + " "
                                + Fmt.plural(k - j, "синю", "сині", "синіх") + " з " + blue + ": $m = " + Fmt.c(red, j) + " \\cdot " + Fmt.c(blue, k - j) + " = " + m + "$.",
                        probFormula(m, n)),
                ExperimentConfig.urn(red, blue, k, false, EventKind.RED_EXACTLY, j));
    }

    static GeneratedTask dieSixAtLeastOnce(RandomGenerator r) {
        int n = between(r, 2, 4);
        long all = (long) Math.pow(6, n);
        long none = (long) Math.pow(5, n);
        String times = n == 2 ? "двічі" : n + " рази";
        return task("die-six-at-least-once", Topic.DICE, 3,
                "Гральний кубик кидають " + times + ". Яка ймовірність того, що шістка випаде хоча б один раз?",
                all - none, all,
                "Перейдіть до протилежної події: «шістка не випала жодного разу». Скільки варіантів для кожного кидка?",
                List.of("Усього наслідків: $n = 6^{" + n + "} = " + all + "$.",
                        "Протилежна подія Ā: у кожному кидку 5 варіантів, тому їй сприяють $5^{" + n + "} = " + none + "$ наслідків.",
                        "Отже, $m = " + all + " - " + none + " = " + (all - none) + "$.",
                        probFormula(all - none, all)),
                ExperimentConfig.dice(n, EventKind.AT_LEAST_ONE_FACE, 6));
    }

    static GeneratedTask urnReplacement(RandomGenerator r) {
        int red = between(r, 2, 5);
        int blue = between(r, 1, 4);
        int k = between(r, 2, 3);
        int j = between(r, 0, k);
        int total = red + blue;
        BigInteger n = Combinatorics.power(total, k);
        BigInteger m = Combinatorics.binomial(k, j).multiply(Combinatorics.power(red, j)).multiply(Combinatorics.power(blue, k - j));
        String text = "В урні " + balls(red, blue) + ". Навмання виймають кулю, записують її колір і повертають назад. Так роблять " + k
                + " " + Fmt.plural(k, "раз", "рази", "разів") + ". Яка ймовірність того, що червона куля "
                + (j == 0 ? "не з'явиться жодного разу" : "з'явиться рівно " + j + " " + Fmt.plural(j, "раз", "рази", "разів")) + "?";
        return task("urn-replacement", Topic.URN, 3, text, m, n,
                "Пронумеруйте кулі. Скільки є впорядкованих записів з " + k + " номерів, якщо кулю щоразу повертають?",
                List.of("Усього наслідків: $n = " + total + "^{" + k + "} = " + n + "$.",
                        "Вибираємо, у яких вийманнях буде червона куля ($" + Fmt.c(k, j) + "$ способів), і які саме кулі: $m = " + Fmt.c(k, j) + " \\cdot "
                                + red + "^{" + j + "} \\cdot " + blue + "^{" + (k - j) + "} = " + m + "$.",
                        probFormula(m, n)),
                ExperimentConfig.urn(red, blue, k, true, EventKind.RED_EXACTLY, j));
    }

    static GeneratedTask coinsMany(RandomGenerator r) {
        int n = between(r, 4, 6);
        int k = between(r, (n + 1) / 2, n - 1);
        BigInteger m = BigInteger.ZERO;
        StringBuilder sum = new StringBuilder();
        for (int i = k; i <= n; i++) {
            m = m.add(Combinatorics.binomial(n, i));
            if (i > k) {
                sum.append(" + ");
            }
            sum.append(Fmt.c(n, i));
        }
        BigInteger all = BigInteger.TWO.pow(n);
        return task("coins-many", Topic.COINS, 3,
                "Кидають " + n + " " + Fmt.plural(n, "монету", "монети", "монет") + ". Яка ймовірність того, що гербів буде не менше, ніж " + k + "?",
                m, all,
                "Розгляньте окремо випадки «рівно " + k + " гербів», «рівно " + (k + 1) + "», … і додайте кількості сприятливих наслідків.",
                List.of("Усього наслідків: $n = 2^{" + n + "} = " + all + "$.",
                        "Сприятливі наслідки: $m = " + sum + " = " + m + "$.", probFormula(m, all)),
                ExperimentConfig.coins(n, EventKind.HEADS_AT_LEAST, k));
    }

    static GeneratedTask phoneTwoDigits(RandomGenerator r) {
        return task("phone-two-digits", Topic.NUMBERS, 3,
                "Абонент забув дві останні цифри номера телефону і пам'ятає лише, що вони різні. Він набирає їх навмання. Яка ймовірність того, що номер буде набрано правильно з першої спроби?",
                1, 90,
                "Порядок цифр важливий, а цифри різні – це розміщення з 10 по 2.",
                List.of("Кількість упорядкованих пар різних цифр: $n = A_{10}^{2} = 10 \\cdot 9 = 90$.", "Правильна лише одна пара: $m = 1$.", probFormula(1, 90)),
                null);
    }

    static GeneratedTask olympiadTeam(RandomGenerator r) {
        int total = between(r, 10, 16);
        int best = between(r, 4, 7);
        int k = between(r, 2, 3);
        BigInteger n = Combinatorics.binomial(total, k);
        BigInteger m = Combinatorics.binomial(best, k);
        String text = "У гуртку " + total + " " + Fmt.plural(total, "учень", "учні", "учнів") + ", з них " + best
                + " – переможці минулорічної олімпіади. Для участі в турнірі навмання обирають " + Fmt.count(k, "учня", "учнів", "учнів")
                + ". Яка ймовірність того, що всі обрані – переможці олімпіади?";
        return task("olympiad-team", Topic.URN, 3, text, m, n,
                "Порядок вибору не важливий: і всі наслідки, і сприятливі – це комбінації.",
                List.of("$n = " + Fmt.c(total, k) + " = " + n + "$.", "$m = " + Fmt.c(best, k) + " = " + m + "$.", probFormula(m, n)),
                ExperimentConfig.urn(best, total - best, k, false, EventKind.RED_EXACTLY, k));
    }

    static GeneratedTask threeDiceSum(RandomGenerator r) {
        int s = between(r, 4, 8);
        int m = 0;
        for (int a = 1; a <= 6; a++) {
            for (int b = 1; b <= 6; b++) {
                for (int c = 1; c <= 6; c++) {
                    if (a + b + c == s) {
                        m++;
                    }
                }
            }
        }
        return task("three-dice-sum", Topic.DICE, 3, "Кидають три гральні кубики. Яка ймовірність того, що сума очок дорівнює " + s + "?", m, 216,
                "Наслідок – упорядкована трійка чисел. Перелічіть трійки з потрібною сумою систематично: спочатку з першим числом 1, потім 2 і т. д.",
                List.of("$n = 6^{3} = 216$.", "Сприятливих упорядкованих трійок із сумою " + s + ": $m = " + m + "$.", probFormula(m, 216)),
                ExperimentConfig.dice(3, EventKind.SUM_EQ, s));
    }

    // ------------------------------------------------------------------ допоміжне

    static int between(RandomGenerator r, int a, int b) {
        return a + r.nextInt(b - a + 1);
    }

    private static String range(int a, int b) {
        StringBuilder sb = new StringBuilder();
        for (int i = a; i <= b; i++) {
            if (i > a) {
                sb.append(", ");
            }
            sb.append(i);
        }
        return sb.toString();
    }

    /** "5 червоних і 3 сині кулі" – узгодження з останнім числівником. */
    static String balls(int red, int blue) {
        return red + " " + Fmt.plural(red, "червона", "червоні", "червоних") + " і " + blue + " "
                + Fmt.plural(blue, "синя", "сині", "синіх") + " " + Fmt.plural(blue, "куля", "кулі", "куль");
    }

    static String probFormula(long m, long n) {
        return probFormula(BigInteger.valueOf(m), BigInteger.valueOf(n));
    }

    static String probFormula(BigInteger m, BigInteger n) {
        Fraction f = Fraction.of(m, n);
        StringBuilder sb = new StringBuilder("$P(A) = ").append(Fmt.frac(m, n));
        if (!(f.numerator().equals(m) && f.denominator().equals(n))) {
            sb.append(" = ").append(Fmt.frac(f));
        }
        sb.append(" ").append(Fmt.approx(f)).append("$.");
        return sb.toString();
    }

    private static GeneratedTask task(String id, Topic topic, int level, String text, long m, long n, String hint,
                                      List<String> solution, ExperimentConfig lab) {
        return task(id, topic, level, text, BigInteger.valueOf(m), BigInteger.valueOf(n), hint, solution, lab);
    }

    private static GeneratedTask task(String id, Topic topic, int level, String text, BigInteger m, BigInteger n, String hint,
                                      List<String> solution, ExperimentConfig lab) {
        return new GeneratedTask(id, topic, level, text, Fraction.of(m, n), hint, solution, lab);
    }
}
