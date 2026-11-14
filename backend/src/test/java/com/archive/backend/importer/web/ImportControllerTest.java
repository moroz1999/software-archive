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
import com.archive.backend.importer.app.StartVektorImportCommand;
import com.archive.backend.importer.app.StartVektorImportHandler;
import com.archive.backend.importer.domain.ImportRunningException;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.importer.read.ImportRunAddedEntry;
import com.archive.backend.importer.read.ImportRunFailedEntry;
import com.archive.backend.importer.read.ImportRunReport;
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
    private StartVektorImportHandler startHandler;

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
                        List.of(new ImportRunFailedEntry("598", "https://vektor-06c.ru/g-dos/", "Timed out")))));

        mvc.perform(get("/api/imports/last-run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.run.section").value("zagruzchiki"))
                .andExpect(jsonPath("$.run.state").value("running"))
                .andExpect(jsonPath("$.run.startedAt").value("2026-11-14T10:00:00Z"))
                .andExpect(jsonPath("$.run.endedAt").isEmpty())
                .andExpect(jsonPath("$.run.known").value(2))
                .andExpect(jsonPath("$.run.addedPrograms[0].softwareId").value(software.toString()))
                .andExpect(jsonPath("$.run.addedPrograms[0].exists").value(true))
                .andExpect(jsonPath("$.run.failedPrograms[0].reason").value("Timed out"));
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

        verify(startHandler).handle(new StartVektorImportCommand(Actor.anonymous(), new VektorSection("zagruzchiki")));
    }

    @Test
    void startsTheImportOfTheWholeSiteWithoutASection() throws Exception {
        mvc.perform(post("/api/imports/vektor")).andExpect(status().isAccepted());

        verify(startHandler).handle(new StartVektorImportCommand(Actor.anonymous(), null));
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
