package com.archive.backend.group.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.label.domain.LabelLetter;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class ListGroupsHandlerTest {

    private final InMemoryGroupReadRepository reads = new InMemoryGroupReadRepository();
    private final ListGroupsHandler handler = new ListGroupsHandler(reads);

    @Test
    void returnsTheRequestedPageOfTheCatalog() {
        UUID group = UUID.randomUUID();
        reads.add(new GroupCatalogEntry(group, "CRW", "Crew Ltd"));
        reads.add(new GroupCatalogEntry(group, "Crew", "Crew Ltd"));

        Page<GroupCatalogEntry> page = handler.handle(new ListGroupsQuery(null, null, null, PageRequest.of(1, 1)));

        assertThat(page.getContent()).containsExactly(new GroupCatalogEntry(group, "Crew", "Crew Ltd"));
        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void asksForTheGivenFilters() {
        UUID country = UUID.randomUUID();
        UUID city = UUID.randomUUID();

        handler.handle(new ListGroupsQuery(
                new LabelLetter("ё"), new CountryId(country), new CityId(city), PageRequest.of(0, 100)));

        assertThat(reads.lastFilter()).isEqualTo(new InMemoryGroupReadRepository.Filter("Е", country, city));
    }

    @Test
    void asksWithoutFiltersWhenNoneIsGiven() {
        handler.handle(new ListGroupsQuery(null, null, null, PageRequest.of(0, 100)));

        assertThat(reads.lastFilter()).isEqualTo(new InMemoryGroupReadRepository.Filter(null, null, null));
    }
}
