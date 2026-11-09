package com.archive.backend.group.web;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.group.app.GetGroupHandler;
import com.archive.backend.group.app.GetGroupQuery;
import com.archive.backend.group.app.ListGroupsHandler;
import com.archive.backend.group.app.ListGroupsQuery;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupNotFoundException;
import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.read.GroupLabel;
import com.archive.backend.group.read.GroupLocation;
import com.archive.backend.group.read.GroupMember;
import com.archive.backend.group.read.RelatedGroup;
import com.archive.backend.group.web.mapper.GroupWebMapperImpl;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelLetter;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GroupController.class)
@Import({SecurityConfig.class, WebConfig.class, GroupWebMapperImpl.class, ApiExceptionHandler.class})
class GroupControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID COUNTRY = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID CITY = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID LABEL = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID PERSON = UUID.fromString("00000000-0000-0000-0000-000000000005");
    private static final UUID INNER = UUID.fromString("00000000-0000-0000-0000-000000000006");
    private static final UUID OUTER = UUID.fromString("00000000-0000-0000-0000-000000000007");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListGroupsHandler listHandler;

    @MockitoBean
    private GetGroupHandler getHandler;

    @Test
    void listsTheCatalogWithoutFilters() throws Exception {
        when(listHandler.handle(new ListGroupsQuery(null, null, null, null, PageRequest.of(0, 100))))
                .thenReturn(new PageImpl<>(
                        List.of(
                                new GroupCatalogEntry(ID, "Crew", "Crew Ltd"),
                                new GroupCatalogEntry(INNER, "Band", null)),
                        PageRequest.of(0, 100),
                        2));

        mvc.perform(get("/api/groups").param("page", "0").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].groupId").value(ID.toString()))
                .andExpect(jsonPath("$.content[0].name").value("Crew"))
                .andExpect(jsonPath("$.content[0].groupName").value("Crew Ltd"))
                .andExpect(jsonPath("$.content[1].groupName").value(nullValue()))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.size").value(100));
    }

    @Test
    void listsTheCatalogWithFilters() throws Exception {
        when(listHandler.handle(new ListGroupsQuery(
                        new LabelLetter("С"),
                        new CountryId(COUNTRY),
                        new CityId(CITY),
                        new PlatformCode("vector"),
                        PageRequest.of(1, 100))))
                .thenReturn(
                        new PageImpl<>(List.of(new GroupCatalogEntry(ID, "Сцена", null)), PageRequest.of(1, 100), 101));

        mvc.perform(get("/api/groups")
                        .param("letter", "с")
                        .param("country", COUNTRY.toString())
                        .param("city", CITY.toString())
                        .param("platform", "vector")
                        .param("page", "1")
                        .param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Сцена"))
                .andExpect(jsonPath("$.page.number").value(1));
    }

    @Test
    void answers400ForAnInvalidLetter() throws Exception {
        mvc.perform(get("/api/groups").param("letter", "AB"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("label.letter-invalid"));
        verify(listHandler, never()).handle(any());
    }

    @Test
    void answers400ForAnInvalidPlatform() throws Exception {
        mvc.perform(get("/api/groups").param("platform", "BK!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.code-invalid"));
    }

    @Test
    void answers400ForAMalformedCountry() throws Exception {
        mvc.perform(get("/api/groups").param("country", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void answers400ForAMalformedCity() throws Exception {
        mvc.perform(get("/api/groups").param("city", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void returnsOneGroup() throws Exception {
        when(getHandler.handle(new GetGroupQuery(new GroupId(ID))))
                .thenReturn(new GroupDetails(
                        ID,
                        "Crew",
                        "Crew Ltd",
                        "Since 1990",
                        List.of("demogroup"),
                        List.of(new GroupLabel(LABEL, "Crew")),
                        List.of(
                                new GroupLocation("Finland", "Helsinki", true),
                                new GroupLocation("Norway", null, false)),
                        List.of(
                                new GroupMember("хакер федя", PERSON, "MASTERSOFT"),
                                new GroupMember("Ghost", null, null)),
                        List.of(new RelatedGroup(INNER, "Band")),
                        List.of(new RelatedGroup(OUTER, "Alpha"))));

        mvc.perform(get("/api/groups/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.name").value("Crew"))
                .andExpect(jsonPath("$.groupName").value("Crew Ltd"))
                .andExpect(jsonPath("$.notes").value("Since 1990"))
                .andExpect(jsonPath("$.types[0]").value("demogroup"))
                .andExpect(jsonPath("$.labels[0].id").value(LABEL.toString()))
                .andExpect(jsonPath("$.labels[0].name").value("Crew"))
                .andExpect(jsonPath("$.locations[0].city").value("Helsinki"))
                .andExpect(jsonPath("$.locations[0].primary").value(true))
                .andExpect(jsonPath("$.locations[1].city").value(nullValue()))
                .andExpect(jsonPath("$.members[0].name").value("хакер федя"))
                .andExpect(jsonPath("$.members[0].personId").value(PERSON.toString()))
                .andExpect(jsonPath("$.members[0].personName").value("MASTERSOFT"))
                .andExpect(jsonPath("$.members[1].personId").value(nullValue()))
                .andExpect(jsonPath("$.memberGroups[0].id").value(INNER.toString()))
                .andExpect(jsonPath("$.memberGroups[0].name").value("Band"))
                .andExpect(jsonPath("$.groups[0].id").value(OUTER.toString()))
                .andExpect(jsonPath("$.groups[0].name").value("Alpha"));
    }

    @Test
    void answers404ForAnUnknownGroup() throws Exception {
        when(getHandler.handle(any(GetGroupQuery.class))).thenThrow(new GroupNotFoundException());

        mvc.perform(get("/api/groups/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("group.not-found"));
    }

    @Test
    void answers400ForAMalformedId() throws Exception {
        mvc.perform(get("/api/groups/{id}", "not-a-uuid")).andExpect(status().isBadRequest());
    }
}
