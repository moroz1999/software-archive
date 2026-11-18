package com.archive.backend.geo.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.shared.domain.Language;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListCitiesHandlerTest {

    private final InMemoryGeoReadRepository reads = new InMemoryGeoReadRepository();
    private final ListCitiesHandler handler = new ListCitiesHandler(reads);

    @Test
    void returnsTheCitiesOfTheCountryInTheLanguageAskedFor() {
        UUID finland = UUID.randomUUID();
        UUID helsinkiId = UUID.randomUUID();
        CityItem helsinki = new CityItem(helsinkiId, "Хельсинки");
        reads.addCountry("ru", new CountryItem(finland, "Финляндия"), helsinki);
        reads.addCountry("en", new CountryItem(finland, "Finland"), new CityItem(helsinkiId, "Helsinki"));
        reads.addCountry("ru", new CountryItem(UUID.randomUUID(), "Норвегия"), new CityItem(UUID.randomUUID(), "Осло"));

        assertThat(handler.handle(new ListCitiesQuery(new CountryId(finland), Language.RU)))
                .containsExactly(helsinki);
    }

    @Test
    void returnsNoCitiesForAnUnknownCountry() {
        assertThat(handler.handle(new ListCitiesQuery(new CountryId(UUID.randomUUID()), Language.EN)))
                .isEmpty();
    }
}
