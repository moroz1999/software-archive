package com.archive.backend.geo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LocationsTest {

    private static final Place FINLAND = Place.restore(new CountryId(UUID.randomUUID()), null);
    private static final Place NORWAY = Place.restore(new CountryId(UUID.randomUUID()), null);

    @Test
    void keepsTheLocationsInTheirOrder() {
        Locations locations = new Locations(List.of(new Location(NORWAY, false), new Location(FINLAND, true)));

        assertThat(locations.all()).containsExactly(new Location(NORWAY, false), new Location(FINLAND, true));
    }

    @Test
    void mayBeNowhere() {
        assertThat(new Locations(List.of()).all()).isEmpty();
    }

    @Test
    void needsAPrimaryLocation() {
        assertThatThrownBy(() -> new Locations(List.of(new Location(NORWAY, false))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("location.primary-one");
                    assertThat(e.field()).isEqualTo("locations");
                });
    }

    @Test
    void hasOnePrimaryLocationOnly() {
        assertThatThrownBy(() -> new Locations(List.of(new Location(NORWAY, true), new Location(FINLAND, true))))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("location.primary-one"));
    }

    @Test
    void keepsOnePlaceOnceWhereItFirstStands() {
        assertThat(new Locations(List.of(
                                new Location(NORWAY, false), new Location(FINLAND, false), new Location(NORWAY, true)))
                        .all())
                .containsExactly(new Location(NORWAY, true), new Location(FINLAND, false));
    }
}
