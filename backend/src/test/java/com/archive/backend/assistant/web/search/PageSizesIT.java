package com.archive.backend.assistant.web.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.TestActors;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * A list the assistant reads is paged as its page on the site is, so the page the editor is on is the page it reads
 * (docs/domain/ai-assistant.md#what-it-knows-of-the-page).
 */
@IntegrationTest
class PageSizesIT {

    private static final ToolContext CONTEXT = new ToolContext(TestActors.MEMBER, Language.EN);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ToolCalls calls;

    @Autowired
    private ObjectMapper json;

    @ParameterizedTest
    @CsvSource(
            delimiter = '|',
            value = {
                "/api/authors?page=0     | list_authors         | {}                  | 100",
                "/api/groups?page=0      | list_groups          | {}                  | 100",
                "/api/software?page=0    | search_software      | {}                  | 30",
                "/api/software/latest    | list_latest_software | {}                  | 12",
                "/api/search?q=elite     | search               | {\"phrase\": \"elite\"} | 50",
            })
    void aListIsPagedAsItsPageOnTheSite(String site, String tool, String arguments, int size) throws Exception {
        mvc.perform(get(site))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(size));

        assertThat(json.valueToTree(calls.read(tool, arguments, CONTEXT))
                        .get("size")
                        .asInt())
                .isEqualTo(size);
    }
}
