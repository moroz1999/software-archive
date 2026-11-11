package com.archive.backend.geo.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlacesTest {

    private static final CountryId FINLAND = new CountryId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final CountryId NORWAY = new CountryId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final CityId HELSINKI = new CityId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    private static final CityId UNKNOWN_CITY = new CityId(UUID.fromString("00000000-0000-0000-0000-000000000099"));

    /** Knows Finland with Helsinki, and Norway without cities. */
    private static final GeoCatalog CATALOG = new GeoCatalog() {
        @Override
        public boolean countryExists(CountryId country) {
            return country.equals(FINLAND) || country.equals(NORWAY);
        }

        @Override
        public Optional<CountryId> countryOf(CityId city) {
            return Optional.ofNullable(Map.of(HELSINKI, FINLAND).get(city));
        }
    };

    private final Places places = new Places(CATALOG);

    @Test
    void buildsACountry() {
        assertThat(places.of(NORWAY, null)).isEqualTo(Place.restore(NORWAY, null));
    }

    @Test
    void buildsACityOfItsCountry() {
        Place place = places.of(FINLAND, HELSINKI);

        assertThat(place.country()).isEqualTo(FINLAND);
        assertThat(place.city()).isEqualTo(HELSINKI);
    }

    @Test
    void buildsACityAloneWithItsCountry() {
        assertThat(places.of(null, HELSINKI)).isEqualTo(Place.restore(FINLAND, HELSINKI));
    }

    @Test
    void refusesAPlaceWithNeitherCountryNorCity() {
        assertThatThrownBy(() -> places.of(null, null))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("location.empty"));
    }

    @Test
    void refusesAnUnknownCountry() {
        assertThatThrownBy(() -> places.of(new CountryId(UUID.randomUUID()), null))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("location.unknown");
                    assertThat(e.field()).isEqualTo("locations");
                });
    }

    @Test
    void refusesAnUnknownCity() {
        assertThatThrownBy(() -> places.of(FINLAND, UNKNOWN_CITY))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("location.unknown"));
    }

    @Test
    void refusesACityOfAnotherCountry() {
        assertThatThrownBy(() -> places.of(NORWAY, HELSINKI))
                .isInstanceOfSatisfying(
                        RuleViolationException.class, e -> assertThat(e.code()).isEqualTo("location.unknown"));
    }

    @Test
    void buildsTheLocationsOfTheDrafts() {
        Locations locations = places.locations(
                List.of(new LocationDraft(FINLAND, HELSINKI, true), new LocationDraft(NORWAY, null, false)));

        assertThat(locations.all())
                .containsExactly(
                        new Location(Place.restore(FINLAND, HELSINKI), true),
                        new Location(Place.restore(NORWAY, null), false));
    }
}
