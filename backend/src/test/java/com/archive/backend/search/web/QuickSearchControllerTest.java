package com.archive.backend.search.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.QuickSearchHandler;
import com.archive.backend.search.app.QuickSearchQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.read.QuickAuthor;
import com.archive.backend.search.read.QuickGroup;
import com.archive.backend.search.read.QuickSearchResult;
import com.archive.backend.search.read.QuickSection;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.web.mapper.QuickSearchWebMapperImpl;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** The quick search over REST (docs/features/search.md#rest). */
@WebMvcTest(QuickSearchController.class)
@Import({SecurityConfig.class, WebConfig.class, QuickSearchWebMapperImpl.class, ApiExceptionHandler.class})
class QuickSearchControllerTest {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ZED = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID CARL = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID SQUAD = UUID.fromString("00000000-0000-0000-0000-000000000021");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private QuickSearchHandler handler;

    @BeforeEach
    void answer() {
        when(handler.handle(any(QuickSearchQuery.class)))
                .thenReturn(new QuickSearchResult(
                        new QuickSection<>(
                                List.of(new SoftwareCatalogItem(ELITE, "Elite", LocalDate.of(1987, 5, 1))), 12),
                        new QuickSection<>(
                                List.of(new QuickAuthor(ZED, "Zed", "John Doe"), new QuickAuthor(CARL, "Carl", null)),
                                2),
                        new QuickSection<>(List.of(new QuickGroup(SQUAD, "ZS")), 1)));
    }

    private QuickSearchQuery asked() {
        ArgumentCaptor<QuickSearchQuery> query = ArgumentCaptor.forClass(QuickSearchQuery.class);
        verify(handler).handle(query.capture());
        return query.getValue();
    }

    @Test
    void answersTheFirstMatchesAndTheCountOfEverySection() throws Exception {
        mvc.perform(get("/api/search/quick").param("q", "z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.software.items[0].id").value(ELITE.toString()))
                .andExpect(jsonPath("$.software.items[0].title").value("Elite"))
                .andExpect(jsonPath("$.software.items[0].releaseDate").value("1987-05-01"))
                .andExpect(jsonPath("$.software.total").value(12))
                .andExpect(jsonPath("$.authors.items[0].id").value(ZED.toString()))
                .andExpect(jsonPath("$.authors.items[0].name").value("Zed"))
                .andExpect(jsonPath("$.authors.items[0].realName").value("John Doe"))
                .andExpect(jsonPath("$.authors.items[1].realName").isEmpty())
                .andExpect(jsonPath("$.authors.total").value(2))
                .andExpect(jsonPath("$.groups.items[0].id").value(SQUAD.toString()))
                .andExpect(jsonPath("$.groups.items[0].name").value("ZS"))
                .andExpect(jsonPath("$.groups.total").value(1));
    }

    @Test
    void asksWithTheKeywordAndThePlatform() throws Exception {
        mvc.perform(get("/api/search/quick").param("q", " zed ").param("platform", "vector"))
                .andExpect(status().isOk());

        assertThat(asked()).isEqualTo(new QuickSearchQuery(new CatalogKeyword("zed"), new PlatformCode("vector")));
    }

    @Test
    void takesAMissingOrBlankKeywordForNone() throws Exception {
        mvc.perform(get("/api/search/quick").param("q", "  ")).andExpect(status().isOk());

        assertThat(asked()).isEqualTo(new QuickSearchQuery(null, null));
    }

    @Test
    void answers400ForAnInvalidPlatform() throws Exception {
        mvc.perform(get("/api/search/quick").param("q", "z").param("platform", "BK!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.code-invalid"));
    }

    @Test
    void answers500WhenTheSearchFails() throws Exception {
        when(handler.handle(any(QuickSearchQuery.class))).thenThrow(new IllegalStateException("Typesense down"));

        mvc.perform(get("/api/search/quick").param("q", "z")).andExpect(status().isInternalServerError());
    }
}
