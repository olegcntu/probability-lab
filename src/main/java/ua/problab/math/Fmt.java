/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Форматування чисел і слів українською мовою. */
public final class Fmt {

    private Fmt() {
    }

    /**
     * Десятковий запис ймовірності з комою: 0,375; для нескінченних дробів — 4 знаки після коми.
     */
    public static String decimal(Fraction f) {
        return decimal(f, 4);
    }

    public static String decimal(Fraction f, int maxDigits) {
        BigDecimal value = new BigDecimal(f.numerator())
                .divide(new BigDecimal(f.denominator()), maxDigits, RoundingMode.HALF_UP)
                .stripTrailingZeros();
        String s = value.scale() < 0 ? value.setScale(0).toPlainString() : value.toPlainString();
        return s.replace('.', ',');
    }

    public static String decimal(double value, int digits) {
        BigDecimal d = BigDecimal.valueOf(value).setScale(digits, RoundingMode.HALF_UP).stripTrailingZeros();
        String s = d.scale() < 0 ? d.setScale(0).toPlainString() : d.toPlainString();
        return s.replace('.', ',');
    }

    /** "= 0,375" для скінченного десяткового дробу, "≈ 0,1667" — для нескінченного. */
    public static String approx(Fraction f) {
        String sign = (f.hasFiniteDecimal() && decimalIsExact(f)) ? "=" : "≈";
        return sign + " " + decimal(f);
    }

    private static boolean decimalIsExact(Fraction f) {
        BigDecimal exact = new BigDecimal(f.numerator()).divide(new BigDecimal(f.denominator()));
        return exact.stripTrailingZeros().scale() <= 4;
    }

    /** Відсоток для пояснення «простими словами»: 37,5 %. */
    public static String percent(Fraction f) {
        return decimal(f.toDouble() * 100, 1) + " %";
    }

    /**
     * Узгодження іменника з числівником: plural(3, "монета", "монети", "монет") → "монети".
     */
    public static String plural(long n, String one, String few, String many) {
        long n100 = Math.abs(n) % 100;
        long n10 = n100 % 10;
        if (n100 >= 11 && n100 <= 14) {
            return many;
        }
        if (n10 == 1) {
            return one;
        }
        if (n10 >= 2 && n10 <= 4) {
            return few;
        }
        return many;
    }

    public static String count(long n, String one, String few, String many) {
        return n + " " + plural(n, one, few, many);
    }

    /** Формула дробу у розмітці, яку відображає веб-інтерфейс: \frac{a}{b}. */
    public static String frac(Object a, Object b) {
        return "\\frac{" + a + "}{" + b + "}";
    }

    /** Дріб у розмітці формул: 3/8 → \frac{3}{8}; ціле → як є. */
    public static String frac(Fraction f) {
        return f.isInteger() ? f.numerator().toString() : frac(f.numerator(), f.denominator());
    }

    /** Число комбінацій у шкільному позначенні: C_{n}^{k}. */
    public static String c(int n, int k) {
        return "C_{" + n + "}^{" + k + "}";
    }
}
