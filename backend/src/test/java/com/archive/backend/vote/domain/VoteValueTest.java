package com.archive.backend.vote.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class VoteValueTest {

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5})
    void isOneToFiveStars(int stars) {
        assertThat(new VoteValue(stars).isWithdrawn()).isFalse();
    }

    @Test
    void noStarsIsAWithdrawnVote() {
        assertThat(new VoteValue(0).isWithdrawn()).isTrue();
        assertThat(VoteValue.WITHDRAWN).isEqualTo(new VoteValue(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 6, 10})
    void refusesAnythingElse(int stars) {
        assertThatThrownBy(() -> new VoteValue(stars))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("vote.value-invalid"));
    }
}
