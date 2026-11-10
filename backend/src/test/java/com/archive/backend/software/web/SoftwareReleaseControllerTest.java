package com.archive.backend.software.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareUnknownException;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.software.app.CreateReleaseCommand;
import com.archive.backend.software.app.CreateReleaseHandler;
import com.archive.backend.software.app.DeleteReleaseCommand;
import com.archive.backend.software.app.DeleteReleaseHandler;
import com.archive.backend.software.app.GetSoftwareReleaseHandler;
import com.archive.backend.software.app.GetSoftwareReleaseQuery;
import com.archive.backend.software.app.UpdateReleaseCommand;
import com.archive.backend.software.app.UpdateReleaseHandler;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDescription;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseNotFoundException;
import com.archive.backend.software.domain.ReleaseTitle;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ReleaseVersion;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.web.mapper.ReleaseWebMapperImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SoftwareReleaseController.class)
@Import({SecurityConfig.class, WebConfig.class, ReleaseWebMapperImpl.class, ApiExceptionHandler.class})
class SoftwareReleaseControllerTest {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID AY_MOD = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final String RELEASES = "/api/software/{softwareId}/releases";
    private static final String RELEASE = RELEASES + "/{releaseId}";
    private static final String AY_MOD_JSON = """
            {"title":"Elite AY","version":"1.1","type":"mod","releaseDate":"1990-03-01",\
            "description":"With AY music","hardware":["ay8910"]}""";
    private static final ReleaseContent AY_MOD_CONTENT = new ReleaseContent(
            new ReleaseTitle("Elite AY"),
            new ReleaseVersion("1.1"),
            ReleaseType.MOD,
            LocalDate.of(1990, 3, 1),
            new ReleaseDescription("With AY music"));
    private static final Set<HardwareCode> AY_MOD_HARDWARE = Set.of(new HardwareCode("ay8910"));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GetSoftwareReleaseHandler getHandler;

    @MockitoBean
    private CreateReleaseHandler createHandler;

    @MockitoBean
    private UpdateReleaseHandler updateHandler;

    @MockitoBean
    private DeleteReleaseHandler deleteHandler;

    @Test
    void returnsAReleaseAsItStatesItself() throws Exception {
        when(getHandler.handle(new GetSoftwareReleaseQuery(new SoftwareId(ELITE), new ReleaseId(AY_MOD))))
                .thenReturn(new ReleaseDetails(
                        AY_MOD,
                        null,
                        "1.1",
                        "mod",
                        LocalDate.of(1990, 3, 1),
                        "With AY music",
                        List.of(new SoftwareHardware("ay8910", "AY-3-8910", "sound"))));

        mvc.perform(get(RELEASE, ELITE, AY_MOD))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(AY_MOD.toString()))
                .andExpect(jsonPath("$.title").isEmpty())
                .andExpect(jsonPath("$.version").value("1.1"))
                .andExpect(jsonPath("$.type").value("mod"))
                .andExpect(jsonPath("$.releaseDate").value("1990-03-01"))
                .andExpect(jsonPath("$.description").value("With AY music"))
                .andExpect(jsonPath("$.hardware[0].code").value("ay8910"))
                .andExpect(jsonPath("$.hardware[0].group").value("sound"));
    }

    @Test
    void answers404ForAnUnknownRelease() throws Exception {
        when(getHandler.handle(any(GetSoftwareReleaseQuery.class))).thenThrow(new ReleaseNotFoundException());

        mvc.perform(get(RELEASE, ELITE, AY_MOD))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("release.not-found"));
    }

    @Test
    void createsARelease() throws Exception {
        when(createHandler.handle(new CreateReleaseCommand(new SoftwareId(ELITE), AY_MOD_CONTENT, AY_MOD_HARDWARE)))
                .thenReturn(new ReleaseId(AY_MOD));

        mvc.perform(post(RELEASES, ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AY_MOD_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/software/" + ELITE + "/releases/" + AY_MOD))
                .andExpect(jsonPath("$.id").value(AY_MOD.toString()));
    }

    @Test
    void createsAReleaseOfOnlyAType() throws Exception {
        when(createHandler.handle(new CreateReleaseCommand(
                        new SoftwareId(ELITE),
                        new ReleaseContent(null, null, ReleaseType.ORIGINAL, null, null),
                        Set.of())))
                .thenReturn(new ReleaseId(AY_MOD));

        mvc.perform(post(RELEASES, ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":null,\"version\":null,\"type\":\"original\",\"hardware\":[]}"))
                .andExpect(status().isCreated());
    }

    @Test
    void answers400ForAMissingType() throws Exception {
        mvc.perform(post(RELEASES, ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hardware\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("release.type-required"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers400ForAnUnknownType() throws Exception {
        mvc.perform(post(RELEASES, ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"beta\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("release.type-invalid"));
    }

    @Test
    void answers400ForATooLongTitleOrVersion() throws Exception {
        mvc.perform(post(RELEASES, ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"mod\",\"title\":\"" + "a".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.too-long"));
        mvc.perform(post(RELEASES, ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"mod\",\"version\":\"" + "1".repeat(51) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("release.version-too-long"));
    }

    @Test
    void answers400ForAMalformedHardwareCode() throws Exception {
        mvc.perform(post(RELEASES, ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"mod\",\"hardware\":[\"AY!\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("hardware.code-invalid"));
    }

    @Test
    void answers422ForHardwareOfAnotherPlatform() throws Exception {
        when(createHandler.handle(any(CreateReleaseCommand.class))).thenThrow(new HardwareUnknownException());

        mvc.perform(post(RELEASES, ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"mod\",\"hardware\":[\"bk0011m\"]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.unknown"));
    }

    @Test
    void answers404WhenAddingToUnknownSoftware() throws Exception {
        when(createHandler.handle(any(CreateReleaseCommand.class))).thenThrow(new SoftwareNotFoundException());

        mvc.perform(post(RELEASES, ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AY_MOD_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void updatesARelease() throws Exception {
        mvc.perform(put(RELEASE, ELITE, AY_MOD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AY_MOD_JSON))
                .andExpect(status().isNoContent());

        verify(updateHandler)
                .handle(new UpdateReleaseCommand(
                        new SoftwareId(ELITE), new ReleaseId(AY_MOD), AY_MOD_CONTENT, AY_MOD_HARDWARE));
    }

    @Test
    void answers404WhenUpdatingAnUnknownRelease() throws Exception {
        doThrow(new ReleaseNotFoundException()).when(updateHandler).handle(any(UpdateReleaseCommand.class));

        mvc.perform(put(RELEASE, ELITE, AY_MOD)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AY_MOD_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("release.not-found"));
    }

    @Test
    void deletesARelease() throws Exception {
        mvc.perform(delete(RELEASE, ELITE, AY_MOD)).andExpect(status().isNoContent());

        verify(deleteHandler).handle(new DeleteReleaseCommand(new SoftwareId(ELITE), new ReleaseId(AY_MOD)));
    }

    @Test
    void answers404WhenDeletingAnUnknownRelease() throws Exception {
        doThrow(new ReleaseNotFoundException()).when(deleteHandler).handle(any(DeleteReleaseCommand.class));

        mvc.perform(delete(RELEASE, ELITE, AY_MOD))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("release.not-found"));
    }

    @Test
    void answers400ForAMalformedReleaseId() throws Exception {
        mvc.perform(get(RELEASE, ELITE, "not-a-uuid")).andExpect(status().isBadRequest());
    }
}
