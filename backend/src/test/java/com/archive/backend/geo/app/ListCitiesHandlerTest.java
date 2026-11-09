package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.CountryItem;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ListCitiesHandlerTest {

    private final InMemoryGeoReadRepository reads = new InMemoryGeoReadRepository();
    private final ListCitiesHandler handler = new ListCitiesHandler(reads);

    @Test
    void returnsTheCitiesOfTheCountry() {
        UUID finland = UUID.randomUUID();
        CityItem helsinki = new CityItem(UUID.randomUUID(), "Helsinki");
        reads.addCountry(new CountryItem(finland, "Finland"), helsinki);
        reads.addCountry(new CountryItem(UUID.randomUUID(), "Norway"), new CityItem(UUID.randomUUID(), "Oslo"));

        assertThat(handler.handle(new ListCitiesQuery(new CountryId(finland)))).containsExactly(helsinki);
    }

    @Test
    void returnsNoCitiesForAnUnknownCountry() {
        assertThat(handler.handle(new ListCitiesQuery(new CountryId(UUID.randomUUID())))).isEmpty();
    }
}
