/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.math;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class CombinatoricsTest {

    @Test
    void binomialCoefficients() {
        assertThat(Combinatorics.binomial(5, 2)).isEqualTo(BigInteger.valueOf(10));
        assertThat(Combinatorics.binomial(8, 3)).isEqualTo(BigInteger.valueOf(56));
        assertThat(Combinatorics.binomial(30, 5)).isEqualTo(BigInteger.valueOf(142_506));
        assertThat(Combinatorics.binomial(4, 0)).isEqualTo(BigInteger.ONE);
        assertThat(Combinatorics.binomial(3, 5)).isEqualTo(BigInteger.ZERO);
    }

    @Test
    void factorialAndArrangements() {
        assertThat(Combinatorics.factorial(5)).isEqualTo(BigInteger.valueOf(120));
        assertThat(Combinatorics.factorial(0)).isEqualTo(BigInteger.ONE);
        assertThat(Combinatorics.arrangements(10, 2)).isEqualTo(BigInteger.valueOf(90));
    }
}
