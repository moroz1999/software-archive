package com.archive.backend.software.web;

import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.app.DeleteSoftwareCommand;
import com.archive.backend.software.app.DeleteSoftwareHandler;
import com.archive.backend.software.app.GetSoftwareQuery;
import com.archive.backend.software.app.GetSoftwareHandler;
import com.archive.backend.software.app.ListSoftwareQuery;
import com.archive.backend.software.app.ListSoftwareHandler;
import com.archive.backend.software.app.UpdateSoftwareCommand;
import com.archive.backend.software.app.UpdateSoftwareHandler;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareSummary;
import com.archive.backend.software.web.mapper.SoftwareWebMapperImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

@WebMvcTest(SoftwareController.class)
@Import({SecurityConfig.class, WebConfig.class, SoftwareWebMapperImpl.class, ApiExceptionHandler.class})
class SoftwareControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListSoftwareHandler listHandler;

    @MockitoBean
    private GetSoftwareHandler getHandler;

    @MockitoBean
    private CreateSoftwareHandler createHandler;

    @MockitoBean
    private UpdateSoftwareHandler updateHandler;

    @MockitoBean
    private DeleteSoftwareHandler deleteHandler;

    @Test
    void listsSoftwareAsListItems() throws Exception {
        when(listHandler.handle(any(ListSoftwareQuery.class)))
                .thenReturn(new PageImpl<>(List.of(new SoftwareSummary(ID, "Title", LocalDate.of(1987, 5, 1))), PageRequest.of(0, 20), 1));

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
    void returnsOneSoftware() throws Exception {
        when(getHandler.handle(new GetSoftwareQuery(new SoftwareId(ID))))
                .thenReturn(new SoftwareDetails(ID, "Elite", "Space trading", LocalDate.of(1985, 9, 20)));

        mvc.perform(get("/api/software/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.title").value("Elite"))
                .andExpect(jsonPath("$.description").value("Space trading"))
                .andExpect(jsonPath("$.releaseDate").value("1985-09-20"));
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
        mvc.perform(get("/api/software/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsSoftware() throws Exception {
        when(createHandler.handle(new CreateSoftwareCommand(new SoftwareTitle("Elite"), new SoftwareDescription("Space trading"), LocalDate.of(1985, 9, 20))))
                .thenReturn(new SoftwareId(ID));

        mvc.perform(post("/api/software").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Elite\",\"description\":\"Space trading\",\"releaseDate\":\"1985-09-20\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/software/" + ID))
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void createsSoftwareWithoutOptionalFields() throws Exception {
        when(createHandler.handle(new CreateSoftwareCommand(new SoftwareTitle("Elite"), null, null))).thenReturn(new SoftwareId(ID));

        mvc.perform(post("/api/software").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Elite\",\"description\":null,\"releaseDate\":null}"))
                .andExpect(status().isCreated());
    }

    @Test
    void answers400ForABlankTitle() throws Exception {
        mvc.perform(post("/api/software").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.required"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers400ForAMissingTitle() throws Exception {
        mvc.perform(post("/api/software").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.required"));
    }

    @Test
    void answers400ForATooLongTitle() throws Exception {
        mvc.perform(post("/api/software").contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"" + "a".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.too-long"));
    }

    @Test
    void answers400ForMalformedJson() throws Exception {
        mvc.perform(post("/api/software").contentType(MediaType.APPLICATION_JSON).content("{\"title\":"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatesSoftware() throws Exception {
        mvc.perform(put("/api/software/{id}", ID).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Elite Plus\",\"description\":null,\"releaseDate\":\"1987-01-01\"}"))
                .andExpect(status().isNoContent());

        verify(updateHandler).handle(new UpdateSoftwareCommand(new SoftwareId(ID), new SoftwareTitle("Elite Plus"), null, LocalDate.of(1987, 1, 1)));
    }

    @Test
    void answers404WhenUpdatingUnknownSoftware() throws Exception {
        doThrow(new SoftwareNotFoundException()).when(updateHandler).handle(any(UpdateSoftwareCommand.class));

        mvc.perform(put("/api/software/{id}", ID).contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Elite\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void deletesSoftware() throws Exception {
        mvc.perform(delete("/api/software/{id}", ID))
                .andExpect(status().isNoContent());

        verify(deleteHandler).handle(new DeleteSoftwareCommand(new SoftwareId(ID)));
    }

    @Test
    void answers404WhenDeletingUnknownSoftware() throws Exception {
        doThrow(new SoftwareNotFoundException()).when(deleteHandler).handle(any(DeleteSoftwareCommand.class));

        mvc.perform(delete("/api/software/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }
}
