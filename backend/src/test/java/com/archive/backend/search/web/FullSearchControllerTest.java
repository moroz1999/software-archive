package com.archive.backend.search.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.FullSearchHandler;
import com.archive.backend.search.app.FullSearchQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.FoundKind;
import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundGroup;
import com.archive.backend.search.read.FoundSet;
import com.archive.backend.search.read.FullSearchResult;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.web.mapper.FullSearchWebMapperImpl;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** The full search over REST (docs/features/search.md#rest). */
@WebMvcTest(FullSearchController.class)
@Import({SecurityConfig.class, WebConfig.class, FullSearchWebMapperImpl.class, ApiExceptionHandler.class})
class FullSearchControllerTest {

    private static final UUID ZED = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID SQUAD = UUID.fromString("00000000-0000-0000-0000-000000000021");
    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FullSearchHandler handler;

    @BeforeEach
    void answer() {
        when(handler.handle(any(FullSearchQuery.class)))
                .thenReturn(new FullSearchResult(
                        1,
                        50,
                        104,
                        List.of(
                                new FoundSet.Authors(52, List.of(new FoundAuthor(ZED, "Zed", "John Doe"))),
                                new FoundSet.Groups(1, List.of(new FoundGroup(SQUAD, "ZS"))),
                                new FoundSet.Software(
                                        51,
                                        List.of(new SoftwareCatalogItem(ELITE, "Elite", LocalDate.of(1987, 5, 1)))))));
    }

    private FullSearchQuery asked() {
        ArgumentCaptor<FullSearchQuery> query = ArgumentCaptor.forClass(FullSearchQuery.class);
        verify(handler).handle(query.capture());
        return query.getValue();
    }

    @Test
    void answersTheSetsOfThePageWithTheirCountsAndThePage() throws Exception {
        mvc.perform(get("/api/search").param("q", "z").param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets[0].type").value("author"))
                .andExpect(jsonPath("$.sets[0].total").value(52))
                .andExpect(jsonPath("$.sets[0].items[0].id").value(ZED.toString()))
                .andExpect(jsonPath("$.sets[0].items[0].name").value("Zed"))
                .andExpect(jsonPath("$.sets[0].items[0].realName").value("John Doe"))
                .andExpect(jsonPath("$.sets[1].type").value("group"))
                .andExpect(jsonPath("$.sets[1].items[0].name").value("ZS"))
                .andExpect(jsonPath("$.sets[2].type").value("software"))
                .andExpect(jsonPath("$.sets[2].items[0].title").value("Elite"))
                .andExpect(jsonPath("$.sets[2].items[0].releaseDate").value("1987-05-01"))
                .andExpect(jsonPath("$.page.size").value(50))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(104))
                .andExpect(jsonPath("$.page.totalPages").value(3));
    }

    @Test
    void asksWithTheKeywordThePlatformTheKindsAndThePage() throws Exception {
        mvc.perform(get("/api/search")
                        .param("q", " zed ")
                        .param("platform", "vector")
                        .param("types", "group,author")
                        .param("page", "2"))
                .andExpect(status().isOk());

        assertThat(asked())
                .isEqualTo(new FullSearchQuery(
                        new CatalogKeyword("zed"),
                        new PlatformCode("vector"),
                        Set.of(FoundKind.AUTHOR, FoundKind.GROUP),
                        PageRequest.of(2, 20)));
    }

    @Test
    void takesNoKeywordNoKindsAndTheFirstPageWhenMissing() throws Exception {
        mvc.perform(get("/api/search").param("q", "  ")).andExpect(status().isOk());

        assertThat(asked()).isEqualTo(new FullSearchQuery(null, null, Set.of(), PageRequest.of(0, 20)));
    }

    @Test
    void answers400ForAnUnknownKind() throws Exception {
        mvc.perform(get("/api/search").param("q", "z").param("types", "author,picture"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("search.type-invalid"));
    }

    @Test
    void answers400ForAnInvalidPlatform() throws Exception {
        mvc.perform(get("/api/search").param("q", "z").param("platform", "BK!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.code-invalid"));
    }

    @Test
    void answers500WhenTheSearchFails() throws Exception {
        when(handler.handle(any(FullSearchQuery.class))).thenThrow(new IllegalStateException("Typesense down"));

        mvc.perform(get("/api/search").param("q", "z")).andExpect(status().isInternalServerError());
    }
}
