package com.archive.backend.geo.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.shared.domain.Language;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListCountriesHandlerTest {

    private final InMemoryGeoReadRepository reads = new InMemoryGeoReadRepository();
    private final ListCountriesHandler handler = new ListCountriesHandler(reads);

    @Test
    void returnsTheCountriesInTheLanguageAskedFor() {
        UUID finlandId = UUID.randomUUID();
        CountryItem finland = new CountryItem(finlandId, "Финляндия");
        CountryItem norway = new CountryItem(UUID.randomUUID(), "Норвегия");
        reads.addCountry("ru", finland);
        reads.addCountry("ru", norway);
        reads.addCountry("en", new CountryItem(finlandId, "Finland"));

        assertThat(handler.handle(new ListCountriesQuery(Language.RU))).containsExactly(finland, norway);
    }
}
