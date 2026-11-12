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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FractionTest {

    @Test
    void reducesAndNormalizesSign() {
        assertThat(Fraction.of(6, 36)).isEqualTo(Fraction.of(1, 6));
        assertThat(Fraction.of(3, -9).toString()).isEqualTo("-1/3");
        assertThat(Fraction.of(0, 5)).isEqualTo(Fraction.ZERO);
        assertThat(Fraction.of(8, 4).toString()).isEqualTo("2");
    }

    @Test
    void arithmetic() {
        assertThat(Fraction.of(1, 6).add(Fraction.of(1, 3))).isEqualTo(Fraction.HALF);
        assertThat(Fraction.ONE.subtract(Fraction.of(5, 6).pow(4))).isEqualTo(Fraction.of(671, 1296));
        assertThat(Fraction.of(2, 3).multiply(Fraction.of(3, 4))).isEqualTo(Fraction.HALF);
        assertThat(Fraction.of(1, 2).divide(Fraction.of(1, 4))).isEqualTo(Fraction.of(2));
        assertThat(Fraction.of(1, 4).complement()).isEqualTo(Fraction.of(3, 4));
    }

    @Test
    void decimalsAreExact() {
        assertThat(Fraction.ofDecimal("0,375")).isEqualTo(Fraction.of(3, 8));
        assertThat(Fraction.ofDecimal("0.3")).isEqualTo(Fraction.of(3, 10));
        assertThat(Fraction.of(3, 8).hasFiniteDecimal()).isTrue();
        assertThat(Fraction.of(1, 6).hasFiniteDecimal()).isFalse();
    }

    @Test
    void formatting() {
        assertThat(Fmt.decimal(Fraction.of(3, 8))).isEqualTo("0,375");
        assertThat(Fmt.decimal(Fraction.of(1, 6))).isEqualTo("0,1667");
        assertThat(Fmt.approx(Fraction.of(1, 6))).isEqualTo("≈ 0,1667");
        assertThat(Fmt.approx(Fraction.of(1, 4))).isEqualTo("= 0,25");
        assertThat(Fmt.plural(3, "монета", "монети", "монет")).isEqualTo("монети");
        assertThat(Fmt.plural(11, "монета", "монети", "монет")).isEqualTo("монет");
        assertThat(Fmt.plural(21, "монета", "монети", "монет")).isEqualTo("монета");
    }

    @Test
    void zeroDenominatorIsRejected() {
        assertThatThrownBy(() -> Fraction.of(BigInteger.ONE, BigInteger.ZERO)).isInstanceOf(ArithmeticException.class);
    }
}
