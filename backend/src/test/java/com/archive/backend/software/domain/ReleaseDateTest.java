package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import java.time.LocalDate;
import java.time.Year;
import org.junit.jupiter.api.Test;

class ReleaseDateTest {

    @Test
    void readsAYear() {
        ReleaseDate date = ReleaseDate.parse("1987");

        assertThat(date).isEqualTo(ReleaseDate.ofYear(Year.of(1987)));
        assertThat(date.day()).isNull();
        assertThat(date.iso()).isEqualTo("1987");
    }

    @Test
    void readsADayOfItsYear() {
        ReleaseDate date = ReleaseDate.parse("1987-05-12");

        assertThat(date.year()).isEqualTo(Year.of(1987));
        assertThat(date.day()).isEqualTo(LocalDate.of(1987, 5, 12));
        assertThat(date.iso()).isEqualTo("1987-05-12");
    }

    @Test
    void refusesWhatIsNeitherAYearNorADay() {
        for (String value : new String[] {"", "87", "19870", "1987-13-01", "1987-02-30", "1987-5-1", "1987 / 2026"}) {
            assertThatThrownBy(() -> ReleaseDate.parse(value))
                    .as(value)
                    .isInstanceOfSatisfying(
                            InvalidValueException.class,
                            e -> assertThat(e.code()).isEqualTo("release-date.invalid"));
        }
    }

    @Test
    void refusesADayOfAnotherYear() {
        assertThatThrownBy(() -> new ReleaseDate(Year.of(1987), LocalDate.of(1988, 1, 1)))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("release-date.invalid"));
    }
}
