/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.practice;

import org.junit.jupiter.api.Test;
import ua.problab.math.Fraction;

import static org.assertj.core.api.Assertions.assertThat;

class AnswerParserTest {

    private static boolean ok(String answer, Fraction expected) {
        return AnswerParser.parse(answer).map(p -> AnswerParser.matches(p, expected)).orElse(false);
    }

    @Test
    void acceptsDifferentNotations() {
        Fraction p = Fraction.of(3, 8);
        assertThat(ok("3/8", p)).isTrue();
        assertThat(ok("6/16", p)).isTrue();
        assertThat(ok("0,375", p)).isTrue();
        assertThat(ok("0.375", p)).isTrue();
        assertThat(ok(",375", p)).isTrue();
        assertThat(ok("37,5%", p)).isTrue();
        assertThat(ok("0,38", p)).isTrue();
    }

    @Test
    void roundedDecimalsForInfiniteFractions() {
        Fraction p = Fraction.of(1, 6);
        assertThat(ok("0,17", p)).isTrue();
        assertThat(ok("0,167", p)).isTrue();
        assertThat(ok("0,16", p)).isFalse();
        assertThat(ok("0,2", p)).isFalse();
        assertThat(ok("17%", p)).isTrue();
    }

    @Test
    void rejectsWrongAndUnreadable() {
        assertThat(ok("3/7", Fraction.of(3, 8))).isFalse();
        assertThat(AnswerParser.parse("три восьмих")).isEmpty();
        assertThat(AnswerParser.parse("1/0")).isEmpty();
        assertThat(AnswerParser.parse("")).isEmpty();
    }
}
