package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorLetter;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ListAuthorsHandlerTest {

    private final InMemoryAuthorReadRepository reads = new InMemoryAuthorReadRepository();
    private final ListAuthorsHandler handler = new ListAuthorsHandler(reads);

    @Test
    void returnsTheRequestedPageOfTheCatalog() {
        UUID person = UUID.randomUUID();
        reads.add(new AuthorCatalogEntry(person, "Axe", "John Doe", "Zed"));
        reads.add(new AuthorCatalogEntry(person, "Zed", "John Doe", null));

        Page<AuthorCatalogEntry> page = handler.handle(new ListAuthorsQuery(null, null, null, PageRequest.of(1, 1)));

        assertThat(page.getContent()).containsExactly(new AuthorCatalogEntry(person, "Zed", "John Doe", null));
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void asksForTheGivenFilters() {
        UUID country = UUID.randomUUID();
        UUID city = UUID.randomUUID();

        handler.handle(new ListAuthorsQuery(new AuthorLetter("ё"), new CountryId(country), new CityId(city), PageRequest.of(0, 100)));

        assertThat(reads.lastFilter()).isEqualTo(new InMemoryAuthorReadRepository.Filter("Е", country, city));
    }

    @Test
    void asksWithoutFiltersWhenNoneIsGiven() {
        handler.handle(new ListAuthorsQuery(null, null, null, PageRequest.of(0, 100)));

        assertThat(reads.lastFilter()).isEqualTo(new InMemoryAuthorReadRepository.Filter(null, null, null));
    }
}
