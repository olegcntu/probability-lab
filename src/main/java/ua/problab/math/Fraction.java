/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

/**
 * Нескоротний раціональний дріб із довільною точністю.
 * Ймовірності в програмі обчислюються точно (як у шкільних задачах: 3/8, 1/6),
 * а десятковий запис використовується лише для відображення.
 */
public final class Fraction implements Comparable<Fraction> {

    public static final Fraction ZERO = new Fraction(BigInteger.ZERO, BigInteger.ONE);
    public static final Fraction ONE = new Fraction(BigInteger.ONE, BigInteger.ONE);
    public static final Fraction HALF = new Fraction(BigInteger.ONE, BigInteger.TWO);

    private final BigInteger num;
    private final BigInteger den;

    private Fraction(BigInteger num, BigInteger den) {
        this.num = num;
        this.den = den;
    }

    public static Fraction of(long num, long den) {
        return of(BigInteger.valueOf(num), BigInteger.valueOf(den));
    }

    public static Fraction of(long value) {
        return of(BigInteger.valueOf(value), BigInteger.ONE);
    }

    public static Fraction of(BigInteger num, BigInteger den) {
        if (den.signum() == 0) {
            throw new ArithmeticException("Знаменник дробу не може дорівнювати нулю");
        }
        if (den.signum() < 0) {
            num = num.negate();
            den = den.negate();
        }
        if (num.signum() == 0) {
            return ZERO;
        }
        BigInteger g = num.gcd(den);
        if (!g.equals(BigInteger.ONE)) {
            num = num.divide(g);
            den = den.divide(g);
        }
        return new Fraction(num, den);
    }

    /**
     * Перетворює десятковий запис на точний дріб: "0,375" → 3/8, "0.3" → 3/10.
     */
    public static Fraction ofDecimal(String text) {
        BigDecimal d = new BigDecimal(text.trim().replace(',', '.'));
        if (d.scale() <= 0) {
            return of(d.toBigIntegerExact(), BigInteger.ONE);
        }
        return of(d.unscaledValue(), BigInteger.TEN.pow(d.scale()));
    }

    public Fraction add(Fraction o) {
        return of(num.multiply(o.den).add(o.num.multiply(den)), den.multiply(o.den));
    }

    public Fraction subtract(Fraction o) {
        return of(num.multiply(o.den).subtract(o.num.multiply(den)), den.multiply(o.den));
    }

    public Fraction multiply(Fraction o) {
        return of(num.multiply(o.num), den.multiply(o.den));
    }

    public Fraction multiply(BigInteger k) {
        return of(num.multiply(k), den);
    }

    public Fraction divide(Fraction o) {
        if (o.num.signum() == 0) {
            throw new ArithmeticException("Ділення на нуль");
        }
        return of(num.multiply(o.den), den.multiply(o.num));
    }

    public Fraction pow(int exponent) {
        if (exponent < 0) {
            throw new IllegalArgumentException("Від'ємний показник степеня");
        }
        return of(num.pow(exponent), den.pow(exponent));
    }

    /** Ймовірність протилежної події: 1 − p. */
    public Fraction complement() {
        return ONE.subtract(this);
    }

    public BigInteger numerator() {
        return num;
    }

    public BigInteger denominator() {
        return den;
    }

    public boolean isZero() {
        return num.signum() == 0;
    }

    public boolean isInteger() {
        return den.equals(BigInteger.ONE);
    }

    public double toDouble() {
        return new BigDecimal(num).divide(new BigDecimal(den), MathContext.DECIMAL64).doubleValue();
    }

    /** Чи має дріб скінченний десятковий запис (знаменник містить лише множники 2 і 5). */
    public boolean hasFiniteDecimal() {
        BigInteger d = den;
        BigInteger five = BigInteger.valueOf(5);
        while (d.mod(BigInteger.TWO).signum() == 0) {
            d = d.divide(BigInteger.TWO);
        }
        while (d.mod(five).signum() == 0) {
            d = d.divide(five);
        }
        return d.equals(BigInteger.ONE);
    }

    @Override
    public int compareTo(Fraction o) {
        return num.multiply(o.den).compareTo(o.num.multiply(den));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Fraction)) {
            return false;
        }
        Fraction f = (Fraction) o;
        return num.equals(f.num) && den.equals(f.den);
    }

    @Override
    public int hashCode() {
        return 31 * num.hashCode() + den.hashCode();
    }

    /** Запис у вигляді "3/8" (або ціле число, якщо знаменник 1). */
    @Override
    public String toString() {
        return isInteger() ? num.toString() : num + "/" + den;
    }
}
