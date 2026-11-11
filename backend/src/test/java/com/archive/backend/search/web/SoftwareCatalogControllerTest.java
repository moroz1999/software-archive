package com.archive.backend.search.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.ListSoftwareCatalogHandler;
import com.archive.backend.search.app.ListSoftwareCatalogQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.search.read.IdCount;
import com.archive.backend.search.read.SoftwareCatalogFacets;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.read.SoftwareCatalogPage;
import com.archive.backend.search.read.YearCount;
import com.archive.backend.search.web.mapper.SoftwareCatalogWebMapperImpl;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** The software catalog over REST (docs/features/software-list.md#rest). */
@WebMvcTest(SoftwareCatalogController.class)
@Import({SecurityConfig.class, WebConfig.class, SoftwareCatalogWebMapperImpl.class, ApiExceptionHandler.class})
class SoftwareCatalogControllerTest {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID GAMES = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final UUID FINLAND = UUID.fromString("00000000-0000-0000-0000-000000000030");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListSoftwareCatalogHandler handler;

    @BeforeEach
    void answer() {
        when(handler.handle(any(ListSoftwareCatalogQuery.class)))
                .thenReturn(new SoftwareCatalogPage(
                        new PageImpl<>(
                                List.of(new SoftwareCatalogItem(ELITE, "Elite", LocalDate.of(1987, 5, 1))),
                                PageRequest.of(0, 20),
                                41),
                        new SoftwareCatalogFacets(
                                List.of(new YearCount(1987, 30), new YearCount(1989, 11)),
                                List.of(new IdCount(GAMES, 41)),
                                List.of(new IdCount(FINLAND, 7)))));
    }

    private ListSoftwareCatalogQuery asked() {
        ArgumentCaptor<ListSoftwareCatalogQuery> query = ArgumentCaptor.forClass(ListSoftwareCatalogQuery.class);
        verify(handler).handle(query.capture());
        return query.getValue();
    }

    @Test
    void answersThePageWithTheValuesOfEveryFilter() throws Exception {
        mvc.perform(get("/api/software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ELITE.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Elite"))
                .andExpect(jsonPath("$.content[0].releaseDate").value("1987-05-01"))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(41))
                .andExpect(jsonPath("$.page.totalPages").value(3))
                .andExpect(jsonPath("$.facets.years[0].value").value(1987))
                .andExpect(jsonPath("$.facets.years[0].count").value(30))
                .andExpect(jsonPath("$.facets.years[1].value").value(1989))
                .andExpect(jsonPath("$.facets.categories[0].value").value(GAMES.toString()))
                .andExpect(jsonPath("$.facets.categories[0].count").value(41))
                .andExpect(jsonPath("$.facets.countries[0].value").value(FINLAND.toString()))
                .andExpect(jsonPath("$.facets.countries[0].count").value(7));
    }

    @Test
    void asksWithoutConditionsWhenNoneIsGiven() throws Exception {
        mvc.perform(get("/api/software")).andExpect(status().isOk());

        assertThat(asked())
                .isEqualTo(new ListSoftwareCatalogQuery(null, null, null, null, null, PageRequest.of(0, 20)));
    }

    @Test
    void asksWithEveryConditionAndThePage() throws Exception {
        mvc.perform(get("/api/software")
                        .param("q", " elite ")
                        .param("platform", "vector")
                        .param("year", "1987")
                        .param("category", GAMES.toString())
                        .param("country", FINLAND.toString())
                        .param("page", "2")
                        .param("size", "20"))
                .andExpect(status().isOk());

        assertThat(asked())
                .isEqualTo(new ListSoftwareCatalogQuery(
                        new CatalogKeyword("elite"),
                        new PlatformCode("vector"),
                        new ReleaseYear(1987),
                        new CategoryId(GAMES),
                        new CountryId(FINLAND),
                        PageRequest.of(2, 20)));
    }

    @Test
    void takesABlankKeywordForNone() throws Exception {
        mvc.perform(get("/api/software").param("q", "  ")).andExpect(status().isOk());

        assertThat(asked().keyword()).isNull();
    }

    @Test
    void answers400ForAnInvalidPlatform() throws Exception {
        mvc.perform(get("/api/software").param("platform", "BK!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.code-invalid"));
    }

    @Test
    void answers400ForAMalformedYearCategoryOrCountry() throws Exception {
        mvc.perform(get("/api/software").param("year", "nineteen")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/software").param("category", "not-a-uuid")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/software").param("country", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void answers500WhenTheSearchFails() throws Exception {
        when(handler.handle(any(ListSoftwareCatalogQuery.class)))
                .thenThrow(new IllegalStateException("Typesense down"));

        mvc.perform(get("/api/software")).andExpect(status().isInternalServerError());
    }
}
