package com.archive.backend.search.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.ListSoftwareCatalogHandler;
import com.archive.backend.search.app.ListSoftwareCatalogQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.search.read.CodeCount;
import com.archive.backend.search.read.HardwareCount;
import com.archive.backend.search.read.IdCount;
import com.archive.backend.search.read.SoftwareCatalogAnswer;
import com.archive.backend.search.read.SoftwareCatalogFacets;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.read.SoftwareCatalogPage;
import com.archive.backend.search.read.YearCount;
import com.archive.backend.search.web.mapper.SoftwareCatalogWebMapperImpl;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.web.ApiExceptionHandler;
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
    private static final UUID TITLE_SCREEN = UUID.fromString("00000000-0000-0000-0055-000000000001");
    private static final UUID GAMES = UUID.fromString("00000000-0000-0000-0000-000000000020");
    private static final UUID FINLAND = UUID.fromString("00000000-0000-0000-0000-000000000030");
    private static final UUID HELSINKI = UUID.fromString("00000000-0000-0000-0000-000000000031");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListSoftwareCatalogHandler handler;

    @BeforeEach
    void answer() {
        when(handler.handle(any(ListSoftwareCatalogQuery.class)))
                .thenReturn(new SoftwareCatalogAnswer(
                        new SoftwareCatalogPage(
                                new PageImpl<>(
                                        List.of(new SoftwareCatalogItem(
                                                ELITE, "Elite", "1987-05-01", List.of(TITLE_SCREEN))),
                                        PageRequest.of(0, 20),
                                        41),
                                new SoftwareCatalogFacets(
                                        List.of(new YearCount(1987, 30), new YearCount(1989, 11)),
                                        List.of(new IdCount(FINLAND, 7)),
                                        List.of(new IdCount(HELSINKI, 4)),
                                        List.of(new CodeCount("vector", 40), new CodeCount("bk", 1)),
                                        List.of(new CodeCount("ay8910", 12)))),
                        List.of(new HardwareCount("ay8910", "AY-3-8910", "sound", "Sound", 12))));
    }

    private ListSoftwareCatalogQuery asked() {
        ArgumentCaptor<ListSoftwareCatalogQuery> query = ArgumentCaptor.forClass(ListSoftwareCatalogQuery.class);
        verify(handler).handle(query.capture());
        return query.getValue();
    }

    @Test
    void answersEveryCardWithTheScreenshotsItShows() throws Exception {
        mvc.perform(get("/api/software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].screenshots[0].id").value(TITLE_SCREEN.toString()))
                .andExpect(jsonPath("$.content[0].screenshots[0].url").value("/api/screenshots/" + TITLE_SCREEN));
    }

    @Test
    void answersThePageWithTheValuesOfTheYearAndCountryFilters() throws Exception {
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
                .andExpect(jsonPath("$.facets.categories").doesNotExist())
                .andExpect(jsonPath("$.facets.countries[0].value").value(FINLAND.toString()))
                .andExpect(jsonPath("$.facets.countries[0].count").value(7));
    }

    @Test
    void asksWithoutConditionsWhenNoneIsGiven() throws Exception {
        mvc.perform(get("/api/software")).andExpect(status().isOk());

        assertThat(asked())
                .isEqualTo(new ListSoftwareCatalogQuery(
                        null, null, null, null, null, PageRequest.of(0, ListSoftwareCatalogQuery.PAGE_SIZE)));
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
    void answersTheValuesOfTheCityPlatformAndHardwareFilters() throws Exception {
        mvc.perform(get("/api/software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facets.cities[0].value").value(HELSINKI.toString()))
                .andExpect(jsonPath("$.facets.cities[0].count").value(4))
                .andExpect(jsonPath("$.facets.platforms[0].value").value("vector"))
                .andExpect(jsonPath("$.facets.platforms[0].count").value(40))
                .andExpect(jsonPath("$.facets.platforms[1].value").value("bk"))
                .andExpect(jsonPath("$.facets.hardware[0].value").value("ay8910"))
                .andExpect(jsonPath("$.facets.hardware[0].name").value("AY-3-8910"))
                .andExpect(jsonPath("$.facets.hardware[0].group").value("sound"))
                .andExpect(jsonPath("$.facets.hardware[0].groupName").value("Sound"))
                .andExpect(jsonPath("$.facets.hardware[0].count").value(12));
    }

    @Test
    void asksForTheCityThePlatformsAndTheHardwareChosen() throws Exception {
        mvc.perform(get("/api/software")
                        .param("country", FINLAND.toString())
                        .param("city", HELSINKI.toString())
                        .param("platforms", "bk,vector")
                        .param("hardware", "ay8910,z80"))
                .andExpect(status().isOk());

        assertThat(asked())
                .isEqualTo(new ListSoftwareCatalogQuery(
                        null,
                        null,
                        null,
                        null,
                        new CountryId(FINLAND),
                        new CityId(HELSINKI),
                        List.of(new PlatformCode("bk"), new PlatformCode("vector")),
                        List.of(new HardwareCode("ay8910"), new HardwareCode("z80")),
                        Language.EN,
                        PageRequest.of(0, ListSoftwareCatalogQuery.PAGE_SIZE)));
    }

    @Test
    void asksForTheHardwareNamedInTheLanguageAskedFor() throws Exception {
        mvc.perform(get("/api/software").header("Accept-Language", "ru")).andExpect(status().isOk());

        assertThat(asked().language()).isEqualTo(Language.RU);
    }

    @Test
    void answers400ForAMalformedCityPlatformsOrHardware() throws Exception {
        mvc.perform(get("/api/software").param("city", "not-a-uuid")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/software").param("platforms", "bk,BK!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.code-invalid"));
        mvc.perform(get("/api/software").param("hardware", "AY 8910"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("hardware.code-invalid"));
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
