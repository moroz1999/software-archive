package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.ConflictException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ScreenshotsTest {

    static Screenshot shot(int n) {
        return new Screenshot(
                new ScreenshotId(UUID.fromString("00000000-0000-0000-0055-%012d".formatted(n))),
                ScreenshotFormat.PNG,
                new ScreenshotFileName("shot-" + n + ".png"),
                100L * n);
    }

    private static final Screenshots ONE_TWO = new Screenshots(List.of(shot(1), shot(2)));

    @Test
    void addsAfterThoseThereAre() {
        assertThat(ONE_TWO.plus(List.of(shot(3), shot(4))).all()).containsExactly(shot(1), shot(2), shot(3), shot(4));
    }

    @Test
    void removesOne() {
        assertThat(ONE_TWO.without(shot(1).id()).all()).containsExactly(shot(2));
    }

    @Test
    void failsToRemoveAScreenshotItDoesNotHave() {
        assertThatThrownBy(() -> ONE_TWO.without(shot(3).id()))
                .isInstanceOfSatisfying(
                        ScreenshotNotFoundException.class,
                        e -> assertThat(e.code()).isEqualTo("screenshot.not-found"));
    }

    @Test
    void putsThemInTheOrderGiven() {
        assertThat(ONE_TWO.reordered(List.of(shot(2).id(), shot(1).id())).all()).containsExactly(shot(2), shot(1));
    }

    @Test
    void refusesAnOrderThatDoesNotNameExactlyTheScreenshots() {
        List<List<ScreenshotId>> outdated = List.of(
                List.of(shot(2).id()),
                List.of(shot(2).id(), shot(1).id(), shot(3).id()),
                List.of(shot(2).id(), shot(3).id()),
                List.of(shot(1).id(), shot(1).id()));
        for (List<ScreenshotId> order : outdated) {
            assertThatThrownBy(() -> ONE_TWO.reordered(order))
                    .isInstanceOfSatisfying(
                            ConflictException.class, e -> assertThat(e.code()).isEqualTo("screenshot.order-outdated"));
        }
    }
}
