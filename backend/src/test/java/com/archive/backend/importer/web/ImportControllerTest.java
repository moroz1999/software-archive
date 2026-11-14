package com.archive.backend.importer.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.importer.app.StartVektorImportCommand;
import com.archive.backend.importer.app.StartVektorImportHandler;
import com.archive.backend.importer.domain.ImportRunningException;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.ChangeForbiddenException;
import com.archive.backend.user.domain.SignInRequiredException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImportController.class)
@Import({SecurityConfig.class, WebConfig.class, ApiExceptionHandler.class})
class ImportControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StartVektorImportHandler startHandler;

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
