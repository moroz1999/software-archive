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
import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundGroup;
import com.archive.backend.search.web.mapper.FullSearchWebMapperImpl;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
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

/** The full search over REST (docs/features/search.md#rest). */
@WebMvcTest(FullSearchController.class)
@Import({SecurityConfig.class, WebConfig.class, FullSearchWebMapperImpl.class, ApiExceptionHandler.class})
class FullSearchControllerTest {

    private static final UUID ZED = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID CARL = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final UUID SQUAD = UUID.fromString("00000000-0000-0000-0000-000000000021");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private FullSearchHandler handler;

    @BeforeEach
    void answer() {
        when(handler.authors(any(FullSearchQuery.class)))
                .thenReturn(new PageImpl<>(
                        List.of(new FoundAuthor(ZED, "Zed", "John Doe"), new FoundAuthor(CARL, "Carl", null)),
                        PageRequest.of(1, 50),
                        52));
        when(handler.groups(any(FullSearchQuery.class)))
                .thenReturn(new PageImpl<>(List.of(new FoundGroup(SQUAD, "ZS")), PageRequest.of(0, 50), 1));
    }

    private FullSearchQuery askedForAuthors() {
        ArgumentCaptor<FullSearchQuery> query = ArgumentCaptor.forClass(FullSearchQuery.class);
        verify(handler).authors(query.capture());
        return query.getValue();
    }

    @Test
    void answersAPageOfPeopleInThePageContract() throws Exception {
        mvc.perform(get("/api/search/authors")
                        .param("q", "z")
                        .param("page", "1")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ZED.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Zed"))
                .andExpect(jsonPath("$.content[0].realName").value("John Doe"))
                .andExpect(jsonPath("$.content[1].realName").isEmpty())
                .andExpect(jsonPath("$.page.size").value(50))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(52))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    void asksForPeopleWithTheKeywordThePlatformAndThePage() throws Exception {
        mvc.perform(get("/api/search/authors")
                        .param("q", " zed ")
                        .param("platform", "vector")
                        .param("page", "1")
                        .param("size", "50"))
                .andExpect(status().isOk());

        assertThat(askedForAuthors())
                .isEqualTo(new FullSearchQuery(
                        new CatalogKeyword("zed"), new PlatformCode("vector"), PageRequest.of(1, 50)));
    }

    @Test
    void takesAMissingOrBlankKeywordForNone() throws Exception {
        mvc.perform(get("/api/search/authors").param("q", "  ").param("size", "50"))
                .andExpect(status().isOk());

        assertThat(askedForAuthors()).isEqualTo(new FullSearchQuery(null, null, PageRequest.of(0, 50)));
    }

    @Test
    void answersAPageOfGroupsInThePageContract() throws Exception {
        mvc.perform(get("/api/search/groups").param("q", "z").param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(SQUAD.toString()))
                .andExpect(jsonPath("$.content[0].name").value("ZS"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void answers400ForAnInvalidPlatform() throws Exception {
        mvc.perform(get("/api/search/groups").param("q", "z").param("platform", "BK!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.code-invalid"));
    }

    @Test
    void answers500WhenTheSearchFails() throws Exception {
        when(handler.groups(any(FullSearchQuery.class))).thenThrow(new IllegalStateException("Typesense down"));

        mvc.perform(get("/api/search/groups").param("q", "z")).andExpect(status().isInternalServerError());
    }
}
