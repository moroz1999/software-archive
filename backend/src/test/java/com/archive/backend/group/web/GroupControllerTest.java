package com.archive.backend.group.web;

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

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.group.app.CreateGroupCommand;
import com.archive.backend.group.app.CreateGroupHandler;
import com.archive.backend.group.app.DeleteGroupCommand;
import com.archive.backend.group.app.DeleteGroupHandler;
import com.archive.backend.group.app.GetGroupHandler;
import com.archive.backend.group.app.GetGroupQuery;
import com.archive.backend.group.app.ListGroupsHandler;
import com.archive.backend.group.app.ListGroupsQuery;
import com.archive.backend.group.app.UpdateGroupCommand;
import com.archive.backend.group.app.UpdateGroupHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupNotFoundException;
import com.archive.backend.group.domain.GroupNotes;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.group.domain.GroupTypeUnknownException;
import com.archive.backend.group.domain.GroupTypesRequiredException;
import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.read.GroupLabel;
import com.archive.backend.group.read.GroupLocation;
import com.archive.backend.group.read.GroupMember;
import com.archive.backend.group.read.RelatedGroup;
import com.archive.backend.group.web.mapper.GroupWebMapperImpl;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelLetter;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
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

    @MockitoBean
    private CreateGroupHandler createHandler;

    @MockitoBean
    private UpdateGroupHandler updateHandler;

    @MockitoBean
    private DeleteGroupHandler deleteHandler;

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
                        List.of(new GroupLabel(LABEL, "Crew", true)),
                        List.of(
                                new GroupLocation(COUNTRY, "Finland", CITY, "Helsinki", true),
                                new GroupLocation(COUNTRY, "Norway", null, null, false)),
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
                .andExpect(jsonPath("$.labels[0].display").value(true))
                .andExpect(jsonPath("$.locations[0].countryId").value(COUNTRY.toString()))
                .andExpect(jsonPath("$.locations[0].cityId").value(CITY.toString()))
                .andExpect(jsonPath("$.locations[1].cityId").value(nullValue()))
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

    private static final String CREW_JSON = """
            {"name":"Crew Ltd","notes":"Since 1990","types":["demogroup","label"],\
            "labels":[{"id":"%s","name":"Crew","display":true},{"id":null,"name":"CRW","display":false}],\
            "locations":[{"country":"%s","city":"%s","primary":true}]}""".formatted(LABEL, COUNTRY, CITY);

    private static final Set<GroupTypeCode> CREW_TYPES =
            Set.of(new GroupTypeCode("demogroup"), new GroupTypeCode("label"));

    private static final List<LabelDraft> CREW_LABELS = List.of(
            new LabelDraft(new LabelId(LABEL), new LabelName("Crew"), true),
            new LabelDraft(null, new LabelName("CRW"), false));

    private static final List<LocationDraft> CREW_LOCATIONS =
            List.of(new LocationDraft(new CountryId(COUNTRY), new CityId(CITY), true));

    @Test
    void createsAGroup() throws Exception {
        when(createHandler.handle(new CreateGroupCommand(
                        new GroupName("Crew Ltd"),
                        new GroupNotes("Since 1990"),
                        CREW_TYPES,
                        CREW_LABELS,
                        CREW_LOCATIONS)))
                .thenReturn(new GroupId(ID));

        mvc.perform(post("/api/groups").contentType(MediaType.APPLICATION_JSON).content(CREW_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/groups/" + ID))
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void createsAGroupOfOnlyANameAndAType() throws Exception {
        when(createHandler.handle(new CreateGroupCommand(
                        new GroupName("Crew Ltd"), null, Set.of(new GroupTypeCode("demogroup")), List.of(), List.of())))
                .thenReturn(new GroupId(ID));

        mvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Crew Ltd\",\"types\":[\"demogroup\"]}"))
                .andExpect(status().isCreated());
    }

    @Test
    void answers400ForAGroupWithoutAName() throws Exception {
        mvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"types\":[\"demogroup\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.required"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers400ForABlankType() throws Exception {
        mvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Crew Ltd\",\"types\":[\" \"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("group.type-code-blank"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers422ForAGroupWithoutTypes() throws Exception {
        when(createHandler.handle(any(CreateGroupCommand.class))).thenThrow(new GroupTypesRequiredException());

        mvc.perform(post("/api/groups").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Crew Ltd\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("group.types-required"))
                .andExpect(jsonPath("$.errors[0].field").value("types"));
    }

    @Test
    void answers422ForAnUnknownType() throws Exception {
        when(createHandler.handle(any(CreateGroupCommand.class))).thenThrow(new GroupTypeUnknownException());

        mvc.perform(post("/api/groups").contentType(MediaType.APPLICATION_JSON).content(CREW_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("group.type-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("types"));
    }

    @Test
    void updatesAGroup() throws Exception {
        mvc.perform(put("/api/groups/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREW_JSON))
                .andExpect(status().isNoContent());

        verify(updateHandler)
                .handle(new UpdateGroupCommand(
                        new GroupId(ID),
                        new GroupName("Crew Ltd"),
                        new GroupNotes("Since 1990"),
                        CREW_TYPES,
                        CREW_LABELS,
                        CREW_LOCATIONS));
    }

    @Test
    void answers404WhenUpdatingAnUnknownGroup() throws Exception {
        doThrow(new GroupNotFoundException()).when(updateHandler).handle(any(UpdateGroupCommand.class));

        mvc.perform(put("/api/groups/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREW_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("group.not-found"));
    }

    @Test
    void deletesAGroup() throws Exception {
        mvc.perform(delete("/api/groups/{id}", ID)).andExpect(status().isNoContent());

        verify(deleteHandler).handle(new DeleteGroupCommand(new GroupId(ID)));
    }

    @Test
    void answers404WhenDeletingAnUnknownGroup() throws Exception {
        doThrow(new GroupNotFoundException()).when(deleteHandler).handle(any(DeleteGroupCommand.class));

        mvc.perform(delete("/api/groups/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("group.not-found"));
    }
}
