package com.archive.backend.geo.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.geo.read.CountryItem;
import java.util.UUID;
import org.junit.jupiter.api.Test;

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
