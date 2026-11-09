package com.archive.backend.geo.app;

import com.archive.backend.geo.read.CountryItem;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ListCountriesHandlerTest {

    private final InMemoryGeoReadRepository reads = new InMemoryGeoReadRepository();
    private final ListCountriesHandler handler = new ListCountriesHandler(reads);

    @Test
    void returnsTheCountries() {
        CountryItem finland = new CountryItem(UUID.randomUUID(), "Finland");
        CountryItem norway = new CountryItem(UUID.randomUUID(), "Norway");
        reads.addCountry(finland);
        reads.addCountry(norway);

        assertThat(handler.handle(new ListCountriesQuery())).containsExactly(finland, norway);
    }
}
