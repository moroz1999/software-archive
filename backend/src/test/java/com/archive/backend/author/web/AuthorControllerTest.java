package com.archive.backend.author.web;

import static org.hamcrest.Matchers.nullValue;
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

import com.archive.backend.author.app.CreateAuthorCommand;
import com.archive.backend.author.app.CreateAuthorHandler;
import com.archive.backend.author.app.DeleteAuthorCommand;
import com.archive.backend.author.app.DeleteAuthorHandler;
import com.archive.backend.author.app.GetAuthorHandler;
import com.archive.backend.author.app.GetAuthorQuery;
import com.archive.backend.author.app.ListAuthorsHandler;
import com.archive.backend.author.app.ListAuthorsQuery;
import com.archive.backend.author.app.UpdateAuthorCommand;
import com.archive.backend.author.app.UpdateAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorNamelessException;
import com.archive.backend.author.domain.AuthorNotFoundException;
import com.archive.backend.author.domain.AuthorNotes;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorGroup;
import com.archive.backend.author.read.AuthorLabel;
import com.archive.backend.author.read.AuthorLocation;
import com.archive.backend.author.web.mapper.AuthorWebMapperImpl;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.geo.domain.LocationUnknownException;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelLetter;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import java.util.List;
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

@WebMvcTest(AuthorController.class)
@Import({SecurityConfig.class, WebConfig.class, AuthorWebMapperImpl.class, ApiExceptionHandler.class})
class AuthorControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID COUNTRY = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CITY = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID LABEL = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID GROUP = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListAuthorsHandler listHandler;

    @MockitoBean
    private GetAuthorHandler getHandler;

    @MockitoBean
    private CreateAuthorHandler createHandler;

    @MockitoBean
    private UpdateAuthorHandler updateHandler;

    @MockitoBean
    private DeleteAuthorHandler deleteHandler;

    @Test
    void listsTheCatalogWithoutFilters() throws Exception {
        when(listHandler.handle(new ListAuthorsQuery(null, null, null, null, PageRequest.of(0, 100))))
                .thenReturn(new PageImpl<>(
                        List.of(
                                new AuthorCatalogEntry(ID, "Axe", "John Doe", "Zed"),
                                new AuthorCatalogEntry(ID, "Zed", "John Doe", null)),
                        PageRequest.of(0, 100),
                        2));

        mvc.perform(get("/api/authors").param("page", "0").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].personId").value(ID.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Axe"))
                .andExpect(jsonPath("$.content[0].realName").value("John Doe"))
                .andExpect(jsonPath("$.content[0].labelOf").value("Zed"))
                .andExpect(jsonPath("$.content[1].labelOf").value(nullValue()))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    void sendsNoRealNameWhenTheEntryIsTheRealName() throws Exception {
        when(listHandler.handle(new ListAuthorsQuery(null, null, null, null, PageRequest.of(0, 100))))
                .thenReturn(new PageImpl<>(
                        List.of(new AuthorCatalogEntry(ID, "Carl Real", null, null)), PageRequest.of(0, 100), 1));

        mvc.perform(get("/api/authors").param("page", "0").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Carl Real"))
                .andExpect(jsonPath("$.content[0].realName").value(nullValue()));
    }

    @Test
    void listsTheCatalogWithFilters() throws Exception {
        when(listHandler.handle(new ListAuthorsQuery(
                        new LabelLetter("Б"),
                        new CountryId(COUNTRY),
                        new CityId(CITY),
                        new PlatformCode("vector"),
                        PageRequest.of(1, 100))))
                .thenReturn(new PageImpl<>(
                        List.of(new AuthorCatalogEntry(ID, "Борис", null, null)), PageRequest.of(1, 100), 101));

        mvc.perform(get("/api/authors")
                        .param("letter", "б")
                        .param("country", COUNTRY.toString())
                        .param("city", CITY.toString())
                        .param("platform", "vector")
                        .param("page", "1")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Борис"))
                .andExpect(jsonPath("$.page.number").value(1));
    }

    @Test
    void answers400ForAnInvalidLetter() throws Exception {
        mvc.perform(get("/api/authors").param("letter", "AB"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("label.letter-invalid"));
        verify(listHandler, never()).handle(any());
    }

    @Test
    void answers400ForAnInvalidPlatform() throws Exception {
        mvc.perform(get("/api/authors").param("platform", "BK!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.code-invalid"));
    }

    @Test
    void answers400ForAMalformedCountry() throws Exception {
        mvc.perform(get("/api/authors").param("country", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void answers400ForAMalformedCity() throws Exception {
        mvc.perform(get("/api/authors").param("city", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void returnsOneAuthor() throws Exception {
        when(getHandler.handle(new GetAuthorQuery(new AuthorId(ID))))
                .thenReturn(new AuthorDetails(
                        ID,
                        "Zed",
                        "John Doe",
                        "Coder since 1984",
                        List.of(new AuthorLabel(LABEL, "Zed", true)),
                        List.of(
                                new AuthorLocation(COUNTRY, "Finland", CITY, "Helsinki", true),
                                new AuthorLocation(COUNTRY, "Norway", null, null, false)),
                        List.of(new AuthorGroup(GROUP, "Crew", "Axe"))));

        mvc.perform(get("/api/authors/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.name").value("Zed"))
                .andExpect(jsonPath("$.realName").value("John Doe"))
                .andExpect(jsonPath("$.notes").value("Coder since 1984"))
                .andExpect(jsonPath("$.labels[0].id").value(LABEL.toString()))
                .andExpect(jsonPath("$.labels[0].name").value("Zed"))
                .andExpect(jsonPath("$.labels[0].display").value(true))
                .andExpect(jsonPath("$.locations[0].countryId").value(COUNTRY.toString()))
                .andExpect(jsonPath("$.locations[0].cityId").value(CITY.toString()))
                .andExpect(jsonPath("$.locations[1].cityId").value(nullValue()))
                .andExpect(jsonPath("$.locations[0].country").value("Finland"))
                .andExpect(jsonPath("$.locations[0].city").value("Helsinki"))
                .andExpect(jsonPath("$.locations[0].primary").value(true))
                .andExpect(jsonPath("$.locations[1].city").value(nullValue()))
                .andExpect(jsonPath("$.groups[0].id").value(GROUP.toString()))
                .andExpect(jsonPath("$.groups[0].name").value("Crew"))
                .andExpect(jsonPath("$.groups[0].as").value("Axe"));
    }

    @Test
    void answers404ForAnUnknownAuthor() throws Exception {
        when(getHandler.handle(any(GetAuthorQuery.class))).thenThrow(new AuthorNotFoundException());

        mvc.perform(get("/api/authors/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("author.not-found"));
    }

    @Test
    void answers400ForAMalformedId() throws Exception {
        mvc.perform(get("/api/authors/{id}", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    private static final String ZED_JSON = """
            {"realName":"John Doe","notes":"Coder since 1984",\
            "labels":[{"id":"%s","name":"Zed","display":true},{"id":null,"name":"Axe","display":false}],\
            "locations":[{"country":"%s","city":"%s","primary":true}]}""".formatted(LABEL, COUNTRY, CITY);

    private static final List<LabelDraft> ZED_LABELS = List.of(
            new LabelDraft(new LabelId(LABEL), new LabelName("Zed"), true),
            new LabelDraft(null, new LabelName("Axe"), false));

    private static final List<LocationDraft> ZED_LOCATIONS =
            List.of(new LocationDraft(new CountryId(COUNTRY), new CityId(CITY), true));

    @Test
    void createsAnAuthor() throws Exception {
        when(createHandler.handle(new CreateAuthorCommand(
                        new RealName("John Doe"), new AuthorNotes("Coder since 1984"), ZED_LABELS, ZED_LOCATIONS)))
                .thenReturn(new AuthorId(ID));

        mvc.perform(post("/api/authors").contentType(MediaType.APPLICATION_JSON).content(ZED_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/authors/" + ID))
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void createsAnAuthorOfOnlyARealName() throws Exception {
        when(createHandler.handle(new CreateAuthorCommand(new RealName("John Doe"), null, List.of(), List.of())))
                .thenReturn(new AuthorId(ID));

        mvc.perform(post("/api/authors").contentType(MediaType.APPLICATION_JSON).content("{\"realName\":\"John Doe\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void answers400ForATooLongRealName() throws Exception {
        mvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"" + "a".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("author.real-name-too-long"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers400ForALabelWithoutAName() throws Exception {
        mvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labels\":[{\"id\":null,\"name\":\" \",\"display\":false}]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.required"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers400ForAMalformedLocation() throws Exception {
        mvc.perform(post("/api/authors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"John Doe\",\"locations\":[{\"country\":\"x\"}]}"))
                .andExpect(status().isBadRequest());
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers422ForAnAuthorWithoutAnyName() throws Exception {
        when(createHandler.handle(any(CreateAuthorCommand.class))).thenThrow(new AuthorNamelessException());

        mvc.perform(post("/api/authors").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("author.nameless"))
                .andExpect(jsonPath("$.errors[0].field").value("realName"));
    }

    @Test
    void answers422ForAnUnknownLocation() throws Exception {
        when(createHandler.handle(any(CreateAuthorCommand.class))).thenThrow(new LocationUnknownException());

        mvc.perform(post("/api/authors").contentType(MediaType.APPLICATION_JSON).content(ZED_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("location.unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("locations"));
    }

    @Test
    void updatesAnAuthor() throws Exception {
        mvc.perform(put("/api/authors/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ZED_JSON))
                .andExpect(status().isNoContent());

        verify(updateHandler)
                .handle(new UpdateAuthorCommand(
                        new AuthorId(ID),
                        new RealName("John Doe"),
                        new AuthorNotes("Coder since 1984"),
                        ZED_LABELS,
                        ZED_LOCATIONS));
    }

    @Test
    void answers404WhenUpdatingAnUnknownAuthor() throws Exception {
        doThrow(new AuthorNotFoundException()).when(updateHandler).handle(any(UpdateAuthorCommand.class));

        mvc.perform(put("/api/authors/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ZED_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("author.not-found"));
    }

    @Test
    void deletesAnAuthor() throws Exception {
        mvc.perform(delete("/api/authors/{id}", ID)).andExpect(status().isNoContent());

        verify(deleteHandler).handle(new DeleteAuthorCommand(new AuthorId(ID)));
    }

    @Test
    void answers404WhenDeletingAnUnknownAuthor() throws Exception {
        doThrow(new AuthorNotFoundException()).when(deleteHandler).handle(any(DeleteAuthorCommand.class));

        mvc.perform(delete("/api/authors/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("author.not-found"));
    }
}
