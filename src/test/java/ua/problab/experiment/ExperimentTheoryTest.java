/*
 * «Лабораторія ймовірностей» — кваліфікаційна робота магістра.
 * Автор: Красавін Олег Олександрович, студент 3 курсу, група 51-фмт,
 * спеціальність А4.04 Середня освіта (Математика),
 * Національний університет «Чернігівський колегіум» імені Т. Г. Шевченка, 2026.
 */
package ua.problab.experiment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/** Перевірка точних ймовірностей на класичних шкільних задачах. */
class ExperimentTheoryTest {

    private static Theory theory(ExperimentConfig cfg) {
        return ExperimentFactory.create(cfg).theory();
    }

    @Test
    void threeCoinsExactlyTwoHeads() {
        Theory t = theory(ExperimentConfig.coins(3, EventKind.HEADS_EXACTLY, 2));
        assertThat(t.probability()).isEqualTo("3/8");
        assertThat(t.favorable()).isEqualTo("3");
        assertThat(t.total()).isEqualTo("8");
        assertThat(t.decimal()).isEqualTo("0,375");
        assertThat(t.outcomes().cells().get(0)).hasSize(8);
    }

    @Test
    void coinsAtLeastOneHeadUsesComplement() {
        Theory t = theory(ExperimentConfig.coins(4, EventKind.HEADS_AT_LEAST, 1));
        assertThat(t.probability()).isEqualTo("15/16");
    }

    @Test
    void allSameCoins() {
        assertThat(theory(ExperimentConfig.coins(3, EventKind.COINS_ALL_SAME, null)).probability()).isEqualTo("1/4");
    }

    @Test
    void biasedCoinUsesBernoulli() {
        ExperimentConfig cfg = new ExperimentConfig(ExperimentType.COINS, 2, "0,3", null, null, null, null, EventKind.HEADS_EXACTLY, 1);
        Theory t = theory(cfg);
        assertThat(t.probability()).isEqualTo("21/50");
        assertThat(t.favorable()).isNull();
    }

    @Test
    void gamblersFallacyStreakStillHalf() {
        Theory t = theory(ExperimentConfig.coins(1, EventKind.HEADS_AFTER_STREAK, 5));
        assertThat(t.probability()).isEqualTo("1/2");
    }

    @Test
    void twoDiceSumSeven() {
        Theory t = theory(ExperimentConfig.dice(2, EventKind.SUM_EQ, 7));
        assertThat(t.probability()).isEqualTo("1/6");
        assertThat(t.favorable()).isEqualTo("6");
        assertThat(t.total()).isEqualTo("36");
        assertThat(t.outcomes().kind()).isEqualTo("grid");
    }

    @Test
    void leibnizElevenVersusTwelve() {
        assertThat(theory(ExperimentConfig.dice(2, EventKind.SUM_EQ, 11)).probability()).isEqualTo("1/18");
        assertThat(theory(ExperimentConfig.dice(2, EventKind.SUM_EQ, 12)).probability()).isEqualTo("1/36");
    }

    @Test
    void deMereAtLeastOneSixInFourRolls() {
        Theory t = theory(ExperimentConfig.dice(4, EventKind.AT_LEAST_ONE_FACE, 6));
        assertThat(t.probability()).isEqualTo("671/1296");
        assertThat(t.value()).isGreaterThan(0.5);
    }

    @Test
    void urnWithoutReplacement() {
        Theory t = theory(ExperimentConfig.urn(5, 3, 3, false, EventKind.RED_EXACTLY, 2));
        assertThat(t.probability()).isEqualTo("15/28");
        assertThat(t.total()).isEqualTo("56");
    }

    @Test
    void urnWithReplacement() {
        Theory t = theory(ExperimentConfig.urn(2, 1, 2, true, EventKind.RED_EXACTLY, 1));
        assertThat(t.probability()).isEqualTo("4/9");
    }

    @Test
    void distributionsSumToOne() {
        ExperimentConfig[] configs = {
                ExperimentConfig.coins(6, EventKind.HEADS_AT_MOST, 2),
                ExperimentConfig.dice(3, EventKind.SUM_AT_LEAST, 12),
                ExperimentConfig.dice(3, EventKind.DICE_ALL_SAME, null),
                ExperimentConfig.urn(7, 4, 4, false, EventKind.URN_ALL_SAME, null),
                ExperimentConfig.urn(3, 3, 5, true, EventKind.RED_AT_LEAST, 3)
        };
        for (ExperimentConfig cfg : configs) {
            double sum = theory(cfg).distribution().probabilities().stream().mapToDouble(Double::doubleValue).sum();
            assertThat(sum).isCloseTo(1.0, within(1e-12));
        }
    }

    @Test
    void invalidParametersAreExplained() {
        assertThatThrownBy(() -> theory(ExperimentConfig.dice(2, EventKind.SUM_EQ, 13)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("від 2 до 12");
        assertThatThrownBy(() -> theory(ExperimentConfig.coins(11, EventKind.HEADS_EXACTLY, 1)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
