package com.archive.backend.author.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoCatalog;
import com.archive.backend.geo.domain.Locations;
import com.archive.backend.geo.domain.Places;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.OwnLabels;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The author and the places the author use case tests work on. */
final class AuthorFixtures {

    static final AuthorId ZED = new AuthorId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    static final LabelId ZED_LABEL = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    static final CountryId FINLAND = new CountryId(UUID.fromString("00000000-0000-0000-0000-000000000021"));
    static final CountryId NORWAY = new CountryId(UUID.fromString("00000000-0000-0000-0000-000000000022"));
    static final CityId HELSINKI = new CityId(UUID.fromString("00000000-0000-0000-0000-000000000031"));

    private AuthorFixtures() {}

    /** Known only by the label Zed, located nowhere. */
    static Author zed() {
        return new Author(
                ZED,
                null,
                null,
                OwnLabels.restore(List.of(new OwnLabel(ZED_LABEL, new LabelName("Zed"))), ZED_LABEL),
                new Locations(List.of()));
    }

    /** Finland with Helsinki, Norway without cities. */
    static Places places() {
        return new Places(new GeoCatalog() {
            @Override
            public boolean countryExists(CountryId country) {
                return country.equals(FINLAND) || country.equals(NORWAY);
            }

            @Override
            public Optional<CountryId> countryOf(CityId city) {
                return city.equals(HELSINKI) ? Optional.of(FINLAND) : Optional.empty();
            }
        });
    }
}
