/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.practice;

import ua.problab.math.Fraction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * Розпізнавання відповіді учня: звичайний дріб (3/8), десятковий дріб (0,375 або 0.375),
 * відсотки (37,5 %), ціле число (0 або 1).
 */
public final class AnswerParser {

    /**
     * @param value    значення відповіді
     * @param exact    true — відповідь записано звичайним дробом або цілим числом
     * @param decimals скільки знаків після коми вказав учень (у частках одиниці)
     */
    public record Parsed(Fraction value, boolean exact, int decimals) {
    }

    private AnswerParser() {
    }

    public static Optional<Parsed> parse(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String s = raw.trim().replace(" ", "").replace(" ", "").replace('−', '-');
        if (s.isEmpty()) {
            return Optional.empty();
        }
        try {
            if (s.endsWith("%")) {
                String body = s.substring(0, s.length() - 1).replace(',', '.');
                BigDecimal d = new BigDecimal(body);
                Fraction f = Fraction.ofDecimal(body).divide(Fraction.of(100));
                return Optional.of(new Parsed(f, false, Math.max(0, d.scale()) + 2));
            }
            if (s.contains("/")) {
                String[] parts = s.split("/");
                if (parts.length != 2) {
                    return Optional.empty();
                }
                long a = Long.parseLong(parts[0]);
                long b = Long.parseLong(parts[1]);
                if (b == 0) {
                    return Optional.empty();
                }
                return Optional.of(new Parsed(Fraction.of(a, b), true, 0));
            }
            String norm = s.replace(',', '.');
            if (norm.startsWith(".")) {
                norm = "0" + norm;
            }
            BigDecimal d = new BigDecimal(norm);
            boolean integer = d.scale() <= 0;
            return Optional.of(new Parsed(Fraction.ofDecimal(norm), integer, Math.max(0, d.scale())));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    /**
     * Чи збігається відповідь з правильною. Звичайний дріб має бути рівним точно (скорочувати не обов'язково),
     * десятковий — збігатися з правильно заокругленим значенням (щонайменше 2 знаки після коми)
     * або відрізнятися менше ніж на 0,0005.
     */
    public static boolean matches(Parsed answer, Fraction expected) {
        if (answer.value().equals(expected)) {
            return true;
        }
        if (answer.exact()) {
            return false;
        }
        double x = answer.value().toDouble();
        double p = expected.toDouble();
        if (answer.decimals() >= 2) {
            BigDecimal rounded = new BigDecimal(expected.numerator())
                    .divide(new BigDecimal(expected.denominator()), answer.decimals(), RoundingMode.HALF_UP);
            if (rounded.compareTo(BigDecimal.valueOf(x).setScale(answer.decimals(), RoundingMode.HALF_UP)) == 0) {
                return true;
            }
        }
        return Math.abs(x - p) < 0.0005;
    }
}
