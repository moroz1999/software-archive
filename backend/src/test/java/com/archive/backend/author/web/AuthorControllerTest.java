package com.archive.backend.author.web;

import com.archive.backend.author.app.GetAuthorHandler;
import com.archive.backend.author.app.GetAuthorQuery;
import com.archive.backend.author.app.ListAuthorsHandler;
import com.archive.backend.author.app.ListAuthorsQuery;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorLetter;
import com.archive.backend.author.domain.AuthorNotFoundException;
import com.archive.backend.author.read.AuthorAlias;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorGroup;
import com.archive.backend.author.read.AuthorLocation;
import com.archive.backend.author.web.mapper.AuthorWebMapperImpl;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthorController.class)
@Import({SecurityConfig.class, WebConfig.class, AuthorWebMapperImpl.class, ApiExceptionHandler.class})
class AuthorControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID COUNTRY = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CITY = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID ALIAS = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID GROUP = UUID.fromString("00000000-0000-0000-0000-000000000005");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListAuthorsHandler listHandler;

    @MockitoBean
    private GetAuthorHandler getHandler;

    @Test
    void listsTheCatalogWithoutFilters() throws Exception {
        when(listHandler.handle(new ListAuthorsQuery(null, null, null, PageRequest.of(0, 100))))
                .thenReturn(new PageImpl<>(List.of(new AuthorCatalogEntry(ID, "Axe", "John Doe", "Zed"), new AuthorCatalogEntry(ID, "Zed", "John Doe", null)), PageRequest.of(0, 100), 2));

        mvc.perform(get("/api/authors").param("page", "0").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].personId").value(ID.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Axe"))
                .andExpect(jsonPath("$.content[0].realName").value("John Doe"))
                .andExpect(jsonPath("$.content[0].aliasOf").value("Zed"))
                .andExpect(jsonPath("$.content[1].aliasOf").value(nullValue()))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    void sendsNoRealNameWhenTheEntryIsTheRealName() throws Exception {
        when(listHandler.handle(new ListAuthorsQuery(null, null, null, PageRequest.of(0, 100))))
                .thenReturn(new PageImpl<>(List.of(new AuthorCatalogEntry(ID, "Carl Real", null, null)), PageRequest.of(0, 100), 1));

        mvc.perform(get("/api/authors").param("page", "0").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Carl Real"))
                .andExpect(jsonPath("$.content[0].realName").value(nullValue()));
    }

    @Test
    void listsTheCatalogWithFilters() throws Exception {
        when(listHandler.handle(new ListAuthorsQuery(new AuthorLetter("Б"), new CountryId(COUNTRY), new CityId(CITY), PageRequest.of(1, 100))))
                .thenReturn(new PageImpl<>(List.of(new AuthorCatalogEntry(ID, "Борис", null, null)), PageRequest.of(1, 100), 101));

        mvc.perform(get("/api/authors")
                        .param("letter", "б").param("country", COUNTRY.toString()).param("city", CITY.toString())
                        .param("page", "1").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Борис"))
                .andExpect(jsonPath("$.page.number").value(1));
    }

    @Test
    void answers400ForAnInvalidLetter() throws Exception {
        mvc.perform(get("/api/authors").param("letter", "AB"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("author.letter-invalid"));
        verify(listHandler, never()).handle(any());
    }

    @Test
    void answers400ForAMalformedCountry() throws Exception {
        mvc.perform(get("/api/authors").param("country", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void answers400ForAMalformedCity() throws Exception {
        mvc.perform(get("/api/authors").param("city", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsOneAuthor() throws Exception {
        when(getHandler.handle(new GetAuthorQuery(new AuthorId(ID)))).thenReturn(new AuthorDetails(
                ID, "Zed", "John Doe", "Coder since 1984",
                List.of(new AuthorAlias(ALIAS, "Zed")),
                List.of(new AuthorLocation("Finland", "Helsinki", true), new AuthorLocation("Norway", null, false)),
                List.of(new AuthorGroup(GROUP, "Crew"))));

        mvc.perform(get("/api/authors/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.name").value("Zed"))
                .andExpect(jsonPath("$.realName").value("John Doe"))
                .andExpect(jsonPath("$.notes").value("Coder since 1984"))
                .andExpect(jsonPath("$.aliases[0].id").value(ALIAS.toString()))
                .andExpect(jsonPath("$.aliases[0].name").value("Zed"))
                .andExpect(jsonPath("$.locations[0].country").value("Finland"))
                .andExpect(jsonPath("$.locations[0].city").value("Helsinki"))
                .andExpect(jsonPath("$.locations[0].primary").value(true))
                .andExpect(jsonPath("$.locations[1].city").value(nullValue()))
                .andExpect(jsonPath("$.groups[0].id").value(GROUP.toString()))
                .andExpect(jsonPath("$.groups[0].name").value("Crew"));
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
        mvc.perform(get("/api/authors/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
