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
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.hardware.domain.PlatformUnknownException;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.app.DeleteSoftwareCommand;
import com.archive.backend.software.app.DeleteSoftwareHandler;
import com.archive.backend.software.app.GetSoftwareHandler;
import com.archive.backend.software.app.GetSoftwareQuery;
import com.archive.backend.software.app.ListLatestSoftwareHandler;
import com.archive.backend.software.app.ListLatestSoftwareQuery;
import com.archive.backend.software.app.ListSoftwareHandler;
import com.archive.backend.software.app.ListSoftwareQuery;
import com.archive.backend.software.app.ListSoftwareReleasesHandler;
import com.archive.backend.software.app.ListSoftwareReleasesQuery;
import com.archive.backend.software.app.UpdateSoftwareCommand;
import com.archive.backend.software.app.UpdateSoftwareHandler;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwarePlatformLockedException;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.read.SoftwarePlatform;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareSummary;
import com.archive.backend.software.web.mapper.SoftwareWebMapperImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SoftwareController.class)
@Import({SecurityConfig.class, WebConfig.class, SoftwareWebMapperImpl.class, ApiExceptionHandler.class})
class SoftwareControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID RELEASE = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final PlatformCode VECTOR = new PlatformCode("vector");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListSoftwareHandler listHandler;

    @MockitoBean
    private ListLatestSoftwareHandler latestHandler;

    @MockitoBean
    private GetSoftwareHandler getHandler;

    @MockitoBean
    private ListSoftwareReleasesHandler releasesHandler;

    @MockitoBean
    private CreateSoftwareHandler createHandler;

    @MockitoBean
    private UpdateSoftwareHandler updateHandler;

    @MockitoBean
    private DeleteSoftwareHandler deleteHandler;

    @Test
    void listsSoftwareAsListItems() throws Exception {
        when(listHandler.handle(any(ListSoftwareQuery.class)))
                .thenReturn(new PageImpl<>(
                        List.of(new SoftwareSummary(ID, "Title", LocalDate.of(1987, 5, 1))), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ID.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Title"))
                .andExpect(jsonPath("$.content[0].releaseDate").value("1987-05-01"))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(20));
    }

    @Test
    void listsTheSoftwareOfAPlatform() throws Exception {
        when(listHandler.handle(new ListSoftwareQuery(new PlatformCode("bk"), PageRequest.of(0, 20))))
                .thenReturn(new PageImpl<>(List.of(new SoftwareSummary(ID, "Title", null)), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/software").param("platform", "bk").param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Title"));
    }

    @Test
    void answers400ForAnInvalidPlatformOfTheList() throws Exception {
        mvc.perform(get("/api/software").param("platform", "BK!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.code-invalid"));
    }

    @Test
    void listsTheLatestSoftwareOfAPlatform() throws Exception {
        when(latestHandler.handle(new ListLatestSoftwareQuery(new PlatformCode("msx"), PageRequest.of(0, 12))))
                .thenReturn(new PageImpl<>(List.of(new SoftwareSummary(ID, "Title", null)), PageRequest.of(0, 12), 1));

        mvc.perform(get("/api/software/latest").param("platform", "msx").param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Title"));
    }

    @Test
    void listsTheLatestSoftware() throws Exception {
        when(latestHandler.handle(new ListLatestSoftwareQuery(null, PageRequest.of(0, 12))))
                .thenReturn(new PageImpl<>(List.of(new SoftwareSummary(ID, "Title", null)), PageRequest.of(0, 12), 1));

        mvc.perform(get("/api/software/latest").param("size", "12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(ID.toString()))
                .andExpect(jsonPath("$.content[0].title").value("Title"))
                .andExpect(jsonPath("$.page.size").value(12));
    }

    @Test
    void returnsOneSoftware() throws Exception {
        when(getHandler.handle(new GetSoftwareQuery(new SoftwareId(ID))))
                .thenReturn(new SoftwareDetails(
                        ID,
                        "Elite",
                        "Space trading",
                        LocalDate.of(1985, 9, 20),
                        new SoftwarePlatform("vector", "Vector"),
                        List.of(new SoftwareHardware("vector06c", "Vector-06C", "computers"))));

        mvc.perform(get("/api/software/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.title").value("Elite"))
                .andExpect(jsonPath("$.description").value("Space trading"))
                .andExpect(jsonPath("$.releaseDate").value("1985-09-20"));
    }

    @Test
    void returnsThePlatformAndTheHardwareOfOneSoftware() throws Exception {
        when(getHandler.handle(new GetSoftwareQuery(new SoftwareId(ID))))
                .thenReturn(new SoftwareDetails(
                        ID,
                        "Elite",
                        null,
                        null,
                        new SoftwarePlatform("vector", "Vector"),
                        List.of(new SoftwareHardware("vector06c", "Vector-06C", "computers"))));

        mvc.perform(get("/api/software/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform.code").value("vector"))
                .andExpect(jsonPath("$.platform.name").value("Vector"))
                .andExpect(jsonPath("$.hardware[0].code").value("vector06c"))
                .andExpect(jsonPath("$.hardware[0].name").value("Vector-06C"))
                .andExpect(jsonPath("$.hardware[0].group").value("computers"));
    }

    @Test
    void listsTheReleasesOfOneSoftware() throws Exception {
        when(releasesHandler.handle(new ListSoftwareReleasesQuery(new SoftwareId(ID))))
                .thenReturn(List.of(new SoftwareRelease(
                        RELEASE,
                        "Elite AY",
                        "1.1",
                        "mod",
                        LocalDate.of(1990, 3, 1),
                        "With AY music",
                        List.of(new SoftwareHardware("ay8910", "AY-3-8910", "sound")))));

        mvc.perform(get("/api/software/{id}/releases", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(RELEASE.toString()))
                .andExpect(jsonPath("$[0].title").value("Elite AY"))
                .andExpect(jsonPath("$[0].version").value("1.1"))
                .andExpect(jsonPath("$[0].type").value("mod"))
                .andExpect(jsonPath("$[0].releaseDate").value("1990-03-01"));
    }

    @Test
    void sendsTheDescriptionAndTheHardwareOfARelease() throws Exception {
        when(releasesHandler.handle(new ListSoftwareReleasesQuery(new SoftwareId(ID))))
                .thenReturn(List.of(new SoftwareRelease(
                        RELEASE,
                        "Elite AY",
                        null,
                        "mod",
                        null,
                        "With AY music",
                        List.of(new SoftwareHardware("ay8910", "AY-3-8910", "sound")))));

        mvc.perform(get("/api/software/{id}/releases", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("With AY music"))
                .andExpect(jsonPath("$[0].hardware[0].code").value("ay8910"))
                .andExpect(jsonPath("$[0].hardware[0].group").value("sound"));
    }

    @Test
    void answers404ForTheReleasesOfUnknownSoftware() throws Exception {
        when(releasesHandler.handle(any(ListSoftwareReleasesQuery.class))).thenThrow(new SoftwareNotFoundException());

        mvc.perform(get("/api/software/{id}/releases", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void answers404ForUnknownSoftware() throws Exception {
        when(getHandler.handle(any(GetSoftwareQuery.class))).thenThrow(new SoftwareNotFoundException());

        mvc.perform(get("/api/software/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void answers400ForAMalformedId() throws Exception {
        mvc.perform(get("/api/software/{id}", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void createsSoftware() throws Exception {
        when(createHandler.handle(new CreateSoftwareCommand(
                        new SoftwareTitle("Elite"),
                        new SoftwareDescription("Space trading"),
                        LocalDate.of(1985, 9, 20),
                        VECTOR,
                        Set.of())))
                .thenReturn(new SoftwareId(ID));

        mvc.perform(
                        post("/api/software")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"title\":\"Elite\",\"description\":\"Space trading\",\"releaseDate\":\"1985-09-20\",\"platform\":\"vector\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/software/" + ID))
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void createsSoftwareWithoutOptionalFields() throws Exception {
        when(createHandler.handle(new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null, VECTOR, Set.of())))
                .thenReturn(new SoftwareId(ID));

        mvc.perform(
                        post("/api/software")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"title\":\"Elite\",\"description\":null,\"releaseDate\":null,\"platform\":\"vector\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void answers400ForAMissingPlatform() throws Exception {
        mvc.perform(post("/api/software")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Elite\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.required"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers400ForAnUnknownPlatform() throws Exception {
        when(createHandler.handle(any(CreateSoftwareCommand.class))).thenThrow(new PlatformUnknownException());

        mvc.perform(post("/api/software")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Elite\",\"platform\":\"zx\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("software.platform-unknown"));
    }

    @Test
    void answers400ForABlankTitle() throws Exception {
        mvc.perform(post("/api/software")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.required"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers400ForAMissingTitle() throws Exception {
        mvc.perform(post("/api/software")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.required"));
    }

    @Test
    void answers400ForATooLongTitle() throws Exception {
        mvc.perform(post("/api/software")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "a".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.too-long"));
    }

    @Test
    void answers400ForMalformedJson() throws Exception {
        mvc.perform(post("/api/software")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatesSoftware() throws Exception {
        mvc.perform(
                        put("/api/software/{id}", ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"title\":\"Elite Plus\",\"description\":null,\"releaseDate\":\"1987-01-01\",\"platform\":\"vector\",\"hardware\":[\"vector06c\",\"ay8910\"]}"))
                .andExpect(status().isNoContent());

        verify(updateHandler)
                .handle(new UpdateSoftwareCommand(
                        new SoftwareId(ID),
                        new SoftwareTitle("Elite Plus"),
                        null,
                        LocalDate.of(1987, 1, 1),
                        VECTOR,
                        Set.of(new HardwareCode("vector06c"), new HardwareCode("ay8910"))));
    }

    @Test
    void answers404WhenUpdatingUnknownSoftware() throws Exception {
        doThrow(new SoftwareNotFoundException()).when(updateHandler).handle(any(UpdateSoftwareCommand.class));

        mvc.perform(put("/api/software/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Elite\",\"platform\":\"vector\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void deletesSoftware() throws Exception {
        mvc.perform(delete("/api/software/{id}", ID)).andExpect(status().isNoContent());

        verify(deleteHandler).handle(new DeleteSoftwareCommand(new SoftwareId(ID)));
    }

    @Test
    void answers404WhenDeletingUnknownSoftware() throws Exception {
        doThrow(new SoftwareNotFoundException()).when(deleteHandler).handle(any(DeleteSoftwareCommand.class));

        mvc.perform(delete("/api/software/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void answers422ForAPlatformLockedByHardware() throws Exception {
        doThrow(new SoftwarePlatformLockedException()).when(updateHandler).handle(any(UpdateSoftwareCommand.class));

        mvc.perform(put("/api/software/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Elite\",\"platform\":\"bk\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("software.platform-locked"))
                .andExpect(jsonPath("$.errors[0].field").value("platform"));
    }

    @Test
    void answers400ForAMalformedHardwareCode() throws Exception {
        mvc.perform(post("/api/software")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Elite\",\"platform\":\"vector\",\"hardware\":[\"AY!\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("hardware.code-invalid"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers422ForHardwareOfAnotherPlatform() throws Exception {
        when(createHandler.handle(any(CreateSoftwareCommand.class))).thenThrow(new HardwareUnknownException());

        mvc.perform(post("/api/software")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Elite\",\"platform\":\"vector\",\"hardware\":[\"bk0011m\"]}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("hardware"));
    }
}
