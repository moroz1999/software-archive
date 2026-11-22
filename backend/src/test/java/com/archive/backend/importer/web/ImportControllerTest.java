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
import com.archive.backend.importer.app.ListBasisCategoriesHandler;
import com.archive.backend.importer.app.ListBasisCategoriesQuery;
import com.archive.backend.importer.app.ListVektorSectionsHandler;
import com.archive.backend.importer.app.ListVektorSectionsQuery;
import com.archive.backend.importer.app.StartImportCommand;
import com.archive.backend.importer.app.StartImportHandler;
import com.archive.backend.importer.domain.ImportRunningException;
import com.archive.backend.importer.domain.basis.BasisCategory;
import com.archive.backend.importer.domain.run.ImportLogSeq;
import com.archive.backend.importer.domain.run.ImportSource;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.importer.read.ImportRunAddedEntry;
import com.archive.backend.importer.read.ImportRunAuthorEntry;
import com.archive.backend.importer.read.ImportRunDownloadedEntry;
import com.archive.backend.importer.read.ImportRunFailedEntry;
import com.archive.backend.importer.read.ImportRunKnownEntry;
import com.archive.backend.importer.read.ImportRunListReadEntry;
import com.archive.backend.importer.read.ImportRunLogLine;
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
    private ListBasisCategoriesHandler basisCategoriesHandler;

    @MockitoBean
    private GetLastImportRunHandler lastRunHandler;

    @Test
    void reportsTheLastRunWithItsLogInTheShapeOfEachKindOfLine() throws Exception {
        UUID software = UUID.fromString("00000000-0000-0000-0001-000000000001");
        Instant at = Instant.parse("2026-11-14T10:01:00Z");
        when(lastRunHandler.handle(new GetLastImportRunQuery(Actor.anonymous(), null)))
                .thenReturn(Optional.of(report(List.of(
                        new ImportRunLogLine(
                                1, at, "list", new ImportRunListReadEntry("https://vektor-06c.ru/list/", 1, 24, 10)),
                        new ImportRunLogLine(
                                2,
                                at,
                                "download",
                                new ImportRunDownloadedEntry("599", "Disk Loader", "archive", "disk.zip", 2048)),
                        new ImportRunLogLine(
                                3, at, "added", new ImportRunAddedEntry("599", software, "Disk Loader", true)),
                        new ImportRunLogLine(
                                4,
                                at,
                                "known",
                                new ImportRunKnownEntry("754", software, "МикроДОС", true, List.of("description"))),
                        new ImportRunLogLine(
                                5,
                                at,
                                "skipped",
                                new ImportRunSkippedEntry(
                                        "524",
                                        "https://vektor-06c.ru/512-demo/",
                                        "512 demo",
                                        "section",
                                        "utility-dlya-pc")),
                        new ImportRunLogLine(
                                6,
                                at,
                                "failed",
                                new ImportRunFailedEntry("598", "https://vektor-06c.ru/g-dos/", "Timed out")),
                        new ImportRunLogLine(
                                7, at, "author", new ImportRunAuthorEntry("label", software, "Графов А.", true, true)),
                        new ImportRunLogLine(8, null, "place", new ImportRunPlaceEntry("city", "Ковров"))))));

        mvc.perform(get("/api/imports/last-run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.run.section").value("zagruzchiki"))
                .andExpect(jsonPath("$.run.state").value("running"))
                .andExpect(jsonPath("$.run.startedAt").value("2026-11-14T10:00:00Z"))
                .andExpect(jsonPath("$.run.endedAt").isEmpty())
                .andExpect(jsonPath("$.run.known").value(2))
                .andExpect(jsonPath("$.run.progress.position").value(4))
                .andExpect(jsonPath("$.run.progress.total").value(12))
                .andExpect(jsonPath("$.run.log[0].seq").value(1))
                .andExpect(jsonPath("$.run.log[0].at").value("2026-11-14T10:01:00Z"))
                .andExpect(jsonPath("$.run.log[0].kind").value("list"))
                .andExpect(jsonPath("$.run.log[0].entry.url").value("https://vektor-06c.ru/list/"))
                .andExpect(jsonPath("$.run.log[0].entry.number").value(1))
                .andExpect(jsonPath("$.run.log[0].entry.pages").value(24))
                .andExpect(jsonPath("$.run.log[0].entry.programs").value(10))
                .andExpect(jsonPath("$.run.log[1].entry.file").value("archive"))
                .andExpect(jsonPath("$.run.log[1].entry.name").value("disk.zip"))
                .andExpect(jsonPath("$.run.log[1].entry.size").value(2048))
                .andExpect(jsonPath("$.run.log[2].entry.softwareId").value(software.toString()))
                .andExpect(jsonPath("$.run.log[2].entry.exists").value(true))
                .andExpect(jsonPath("$.run.log[3].entry.supplements[0]").value("description"))
                .andExpect(jsonPath("$.run.log[4].entry.reason").value("section"))
                .andExpect(jsonPath("$.run.log[4].entry.detail").value("utility-dlya-pc"))
                .andExpect(jsonPath("$.run.log[5].entry.reason").value("Timed out"))
                .andExpect(jsonPath("$.run.log[6].entry.kind").value("label"))
                .andExpect(jsonPath("$.run.log[6].entry.created").value(true))
                .andExpect(jsonPath("$.run.log[7].at").isEmpty())
                .andExpect(jsonPath("$.run.log[7].entry.name").value("Ковров"));
    }

    @Test
    void asksForTheLinesOfTheLogAfterTheOneGiven() throws Exception {
        when(lastRunHandler.handle(new GetLastImportRunQuery(Actor.anonymous(), new ImportLogSeq(41))))
                .thenReturn(Optional.of(report(List.of())));

        mvc.perform(get("/api/imports/last-run").param("after", "41"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.run.log").isEmpty());
    }

    private static ImportRunReport report(List<ImportRunLogLine> log) {
        return new ImportRunReport(
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
                new ImportRunProgressEntry(4, 12, "https://vektor-06c.ru/512-demo/"),
                log);
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
    void listsTheCategoriesOfBasisThatCanBeImported() throws Exception {
        when(basisCategoriesHandler.handle(new ListBasisCategoriesQuery(Actor.anonymous())))
                .thenReturn(List.of("arcade", "demo"));

        mvc.perform(get("/api/imports/basis/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("arcade"))
                .andExpect(jsonPath("$[1]").value("demo"));
    }

    @Test
    void forbidsTheBasisCategoriesToAMemberWhoIsNotAnAdministrator() throws Exception {
        doThrow(new ChangeForbiddenException()).when(basisCategoriesHandler).handle(any());

        mvc.perform(get("/api/imports/basis/categories")).andExpect(status().isForbidden());
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
    void startsTheImportOfOneCategoryOfBasis() throws Exception {
        mvc.perform(post("/api/imports/basis").param("category", "arcade")).andExpect(status().isAccepted());

        verify(startHandler)
                .handle(new StartImportCommand(Actor.anonymous(), ImportSource.BASIS, new BasisCategory("arcade")));
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
