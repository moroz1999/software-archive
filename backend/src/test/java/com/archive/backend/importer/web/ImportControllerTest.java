package com.archive.backend.importer.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.importer.app.GetLastImportRunHandler;
import com.archive.backend.importer.app.GetLastImportRunQuery;
import com.archive.backend.importer.app.ListVektorSectionsHandler;
import com.archive.backend.importer.app.ListVektorSectionsQuery;
import com.archive.backend.importer.app.StartImportCommand;
import com.archive.backend.importer.app.StartImportHandler;
import com.archive.backend.importer.domain.ImportRunningException;
import com.archive.backend.importer.domain.run.ImportSource;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.importer.read.ImportRunAddedEntry;
import com.archive.backend.importer.read.ImportRunAuthorEntry;
import com.archive.backend.importer.read.ImportRunFailedEntry;
import com.archive.backend.importer.read.ImportRunKnownEntry;
import com.archive.backend.importer.read.ImportRunPlaceEntry;
import com.archive.backend.importer.read.ImportRunProgressEntry;
import com.archive.backend.importer.read.ImportRunReport;
import com.archive.backend.importer.read.ImportRunSkippedEntry;
import com.archive.backend.importer.web.mapper.ImportWebMapperImpl;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImportController.class)
@Import({SecurityConfig.class, WebConfig.class, ImportWebMapperImpl.class, ApiExceptionHandler.class})
class ImportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StartImportHandler startHandler;

    @MockitoBean
    private ListVektorSectionsHandler sectionsHandler;

    @MockitoBean
    private GetLastImportRunHandler lastRunHandler;

    @Test
    void reportsTheLastRunWithWhatEachProgramCameTo() throws Exception {
        UUID software = UUID.fromString("00000000-0000-0000-0001-000000000001");
        when(lastRunHandler.handle(new GetLastImportRunQuery(Actor.anonymous())))
                .thenReturn(Optional.of(new ImportRunReport(
                        UUID.fromString("00000000-0000-0000-0000-00000000000a"),
                        "vektor-06c.ru",
                        "zagruzchiki",
                        "RUNNING",
                        Instant.parse("2026-11-14T10:00:00Z"),
                        null,
                        1,
                        2,
                        0,
                        1,
                        List.of(new ImportRunAddedEntry("599", software, "Disk Loader", true)),
                        List.of(new ImportRunFailedEntry("598", "https://vektor-06c.ru/g-dos/", "Timed out")),
                        List.of(new ImportRunKnownEntry("754", software, "МикроДОС", true, List.of("description"))),
                        List.of(new ImportRunSkippedEntry(
                                "524", "https://vektor-06c.ru/512-demo/", "512 demo", "section", "utility-dlya-pc")),
                        List.of(new ImportRunAuthorEntry("label", software, "Графов А.", true, true)),
                        List.of(new ImportRunPlaceEntry("city", "Ковров")),
                        new ImportRunProgressEntry(4, 12, "https://vektor-06c.ru/512-demo/"))));

        mvc.perform(get("/api/imports/last-run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.run.section").value("zagruzchiki"))
                .andExpect(jsonPath("$.run.state").value("running"))
                .andExpect(jsonPath("$.run.startedAt").value("2026-11-14T10:00:00Z"))
                .andExpect(jsonPath("$.run.endedAt").isEmpty())
                .andExpect(jsonPath("$.run.known").value(2))
                .andExpect(jsonPath("$.run.addedPrograms[0].softwareId").value(software.toString()))
                .andExpect(jsonPath("$.run.addedPrograms[0].exists").value(true))
                .andExpect(jsonPath("$.run.failedPrograms[0].reason").value("Timed out"))
                .andExpect(jsonPath("$.run.knownPrograms[0].supplements[0]").value("description"))
                .andExpect(jsonPath("$.run.skippedPrograms[0].reason").value("section"))
                .andExpect(jsonPath("$.run.skippedPrograms[0].detail").value("utility-dlya-pc"))
                .andExpect(jsonPath("$.run.authors[0].kind").value("label"))
                .andExpect(jsonPath("$.run.authors[0].created").value(true))
                .andExpect(jsonPath("$.run.places[0].name").value("Ковров"))
                .andExpect(jsonPath("$.run.progress.position").value(4))
                .andExpect(jsonPath("$.run.progress.total").value(12));
    }

    @Test
    void reportsNoRunBeforeTheFirst() throws Exception {
        when(lastRunHandler.handle(any())).thenReturn(Optional.empty());

        mvc.perform(get("/api/imports/last-run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.run").isEmpty());
    }

    @Test
    void forbidsTheReportToAMemberWhoIsNotAnAdministrator() throws Exception {
        doThrow(new ChangeForbiddenException()).when(lastRunHandler).handle(any());

        mvc.perform(get("/api/imports/last-run")).andExpect(status().isForbidden());
    }

    @Test
    void listsTheSectionsToImportByTheirCodes() throws Exception {
        when(sectionsHandler.handle(new ListVektorSectionsQuery(Actor.anonymous())))
                .thenReturn(List.of("arkadnye-igry", "zagruzchiki"));

        mvc.perform(get("/api/imports/vektor/sections"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("arkadnye-igry"))
                .andExpect(jsonPath("$[1]").value("zagruzchiki"));
    }

    @Test
    void forbidsTheSectionsToAMemberWhoIsNotAnAdministrator() throws Exception {
        doThrow(new ChangeForbiddenException()).when(sectionsHandler).handle(any());

        mvc.perform(get("/api/imports/vektor/sections")).andExpect(status().isForbidden());
    }

    @Test
    void startsTheImportOfASectionAndAnswersAccepted() throws Exception {
        mvc.perform(post("/api/imports/vektor").param("section", "zagruzchiki")).andExpect(status().isAccepted());

        verify(startHandler)
                .handle(new StartImportCommand(
                        Actor.anonymous(), ImportSource.VEKTOR, new VektorSection("zagruzchiki")));
    }

    @Test
    void startsTheImportOfTheWholeSiteWithoutASection() throws Exception {
        mvc.perform(post("/api/imports/vektor")).andExpect(status().isAccepted());

        verify(startHandler).handle(new StartImportCommand(Actor.anonymous(), ImportSource.VEKTOR, null));
    }

    @Test
    void startsTheImportOfTheCardIndexOfBasis() throws Exception {
        mvc.perform(post("/api/imports/basis")).andExpect(status().isAccepted());

        verify(startHandler).handle(new StartImportCommand(Actor.anonymous(), ImportSource.BASIS, null));
    }

    @Test
    void answersConflictWhileAnImportRuns() throws Exception {
        doThrow(new ImportRunningException()).when(startHandler).handle(any());

        mvc.perform(post("/api/imports/vektor"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("import.running"));
    }

    @Test
    void asksAPublicVisitorToSignIn() throws Exception {
        doThrow(new SignInRequiredException()).when(startHandler).handle(any());

        mvc.perform(post("/api/imports/vektor")).andExpect(status().isUnauthorized());
    }

    @Test
    void forbidsAMemberWhoIsNotAnAdministrator() throws Exception {
        doThrow(new ChangeForbiddenException()).when(startHandler).handle(any());

        mvc.perform(post("/api/imports/vektor")).andExpect(status().isForbidden());
    }
}
