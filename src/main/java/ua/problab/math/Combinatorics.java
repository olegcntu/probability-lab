/*
 * «Лабораторія ймовірностей» – кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 2 курсу магістратури, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.math;

import java.math.BigInteger;

/** Шкільні формули комбінаторики. */
public final class Combinatorics {

    private Combinatorics() {
    }

    /** n! */
    public static BigInteger factorial(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Факторіал визначено лише для невід'ємних чисел");
        }
        BigInteger r = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            r = r.multiply(BigInteger.valueOf(i));
        }
        return r;
    }

    /** Число комбінацій C(n, k) – скількома способами можна вибрати k елементів з n (порядок не важливий). */
    public static BigInteger binomial(int n, int k) {
        if (n < 0 || k < 0 || k > n) {
            return BigInteger.ZERO;
        }
        int kk = Math.min(k, n - k);
        BigInteger r = BigInteger.ONE;
        for (int i = 1; i <= kk; i++) {
            r = r.multiply(BigInteger.valueOf(n - kk + i)).divide(BigInteger.valueOf(i));
        }
        return r;
    }

    /** Число розміщень A(n, k) = n·(n−1)·…·(n−k+1). */
    public static BigInteger arrangements(int n, int k) {
        if (n < 0 || k < 0 || k > n) {
            return BigInteger.ZERO;
        }
        BigInteger r = BigInteger.ONE;
        for (int i = 0; i < k; i++) {
            r = r.multiply(BigInteger.valueOf(n - i));
        }
        return r;
    }

    public static BigInteger power(long base, int exponent) {
        return BigInteger.valueOf(base).pow(exponent);
    }
}
