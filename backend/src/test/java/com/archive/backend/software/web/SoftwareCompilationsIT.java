package com.archive.backend.software.web;

import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.OTHER;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.session.AccessTokens;
import com.jayway.jsonpath.JsonPath;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class SoftwareCompilationsIT {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AccessTokens accessTokens;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void storeEditors() {
        TestActors.store(jdbc);
    }

    @Test
    void findsExistingProgramsByTitleForTheCompilationEditor() throws Exception {
        String program = create("Galactic Bowling", null);

        mvc.perform(get("/api/software/lookup").param("query", "bowling"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(program))
                .andExpect(jsonPath("$.items[0].title").value("Galactic Bowling"));
    }

    @Test
    void showsTheContentsAndTheCompilationsTheProgramAppearsIn() throws Exception {
        String program = create("Galactic Bowling", null);
        String compilation = create("Arcade Collection", items(program));

        details(compilation)
                .andExpect(jsonPath("$.compilationItems[0].id").value(program))
                .andExpect(jsonPath("$.compilationItems[0].title").value("Galactic Bowling"));
        details(program)
                .andExpect(jsonPath("$.compilations[0].id").value(compilation))
                .andExpect(jsonPath("$.compilations[0].title").value("Arcade Collection"));
    }

    @Test
    void replacesTheContentsOnlyWhenTheEditIncludesTheField() throws Exception {
        String program = create("Galactic Bowling", null);
        String compilation = create("Arcade Collection", items(program));

        update(compilation, "Arcade Collection II", null).andExpect(status().isNoContent());
        details(compilation).andExpect(jsonPath("$.compilationItems[0].id").value(program));

        update(compilation, "Arcade Collection II", "[]").andExpect(status().isNoContent());
        details(compilation).andExpect(jsonPath("$.compilationItems").isEmpty());
        details(program).andExpect(jsonPath("$.compilations").isEmpty());
    }

    @Test
    void deletingAProgramCleansUpItsCompilationLinksInBothDirections() throws Exception {
        String program = create("Galactic Bowling", null);
        String compilation = create("Arcade Collection", items(program));
        String biggerCompilation = create("Games of the Year", items(compilation));

        mvc.perform(delete("/api/software/{id}", compilation).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(status().isNoContent());

        details(program).andExpect(jsonPath("$.compilations").isEmpty());
        details(biggerCompilation).andExpect(jsonPath("$.compilationItems").isEmpty());
        mvc.perform(get("/api/software/{id}/compilation-items", compilation))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void refusesADeletedProgramUnderTheCompilationField() throws Exception {
        String deleted = create("Galactic Bowling", null);
        String compilation = create("Arcade Collection", null);
        mvc.perform(delete("/api/software/{id}", deleted).with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(status().isNoContent());

        update(compilation, "Arcade Collection", items(deleted))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].field").value("compilationItemIds"))
                .andExpect(jsonPath("$.errors[0].code").value("software.compilation-item-unknown"));
    }

    @Test
    void refusesAProgramInItsOwnContents() throws Exception {
        String compilation = create("Arcade Collection", null);

        update(compilation, "Arcade Collection", items(compilation))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].field").value("compilationItemIds"))
                .andExpect(jsonPath("$.errors[0].code").value("software.compilation-cycle"));
    }

    @Test
    void refusesAnIndirectCycleThroughSeveralCompilations() throws Exception {
        String first = create("First Collection", null);
        String second = create("Second Collection", items(first));
        String third = create("Third Collection", items(second));

        update(first, "First Collection", items(third))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errors[0].field").value("compilationItemIds"))
                .andExpect(jsonPath("$.errors[0].code").value("software.compilation-cycle"));
    }

    @Test
    void editingTheContentsRequiresPermissionToEditTheCompilation() throws Exception {
        String compilation = create("Arcade Collection", null);
        String program = create("Galactic Bowling", null);

        mvc.perform(put("/api/software/{id}", compilation)
                        .with(TestActors.bearer(accessTokens, OTHER))
                        .contentType("application/json")
                        .content(request("Arcade Collection", items(program))))
                .andExpect(status().isForbidden());
    }

    private String create(String title, @Nullable String contents) throws Exception {
        String created = mvc.perform(post("/api/software")
                        .with(TestActors.bearer(accessTokens, MEMBER))
                        .contentType("application/json")
                        .content(request(title, contents)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return JsonPath.read(created, "$.id");
    }

    private ResultActions update(String id, String title, @Nullable String contents) throws Exception {
        return mvc.perform(put("/api/software/{id}", id)
                .with(TestActors.bearer(accessTokens, MEMBER))
                .contentType("application/json")
                .content(request(title, contents)));
    }

    private ResultActions details(String id) throws Exception {
        return mvc.perform(get("/api/software/{id}", id)).andExpect(status().isOk());
    }

    private static String request(String title, @Nullable String contents) {
        return """
                {"title":"%s","platform":"vector"%s}
                """.formatted(title, contents == null ? "" : ",\"compilationItemIds\":" + contents);
    }

    private static String items(String id) {
        return "[\"%s\"]".formatted(id);
    }
}
