package com.archive.backend.category.web;

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

import com.archive.backend.category.app.CreateCategoryCommand;
import com.archive.backend.category.app.CreateCategoryHandler;
import com.archive.backend.category.app.DeleteCategoryCommand;
import com.archive.backend.category.app.DeleteCategoryHandler;
import com.archive.backend.category.app.GetCategoryHandler;
import com.archive.backend.category.app.GetCategoryQuery;
import com.archive.backend.category.app.ListCategoriesHandler;
import com.archive.backend.category.app.ListCategoriesQuery;
import com.archive.backend.category.app.UpdateCategoryCommand;
import com.archive.backend.category.app.UpdateCategoryHandler;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryNotFoundException;
import com.archive.backend.category.domain.CategoryParentInsideException;
import com.archive.backend.category.domain.CategoryParentUnknownException;
import com.archive.backend.category.domain.CategoryTitle;
import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.category.read.CategoryNode;
import com.archive.backend.category.web.mapper.CategoryWebMapperImpl;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
@Import({SecurityConfig.class, WebConfig.class, CategoryWebMapperImpl.class, ApiExceptionHandler.class})
class CategoryControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID GAMES = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID SHOOTERS = UUID.fromString("00000000-0000-0000-0000-000000000003");

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListCategoriesHandler listHandler;

    @MockitoBean
    private GetCategoryHandler getHandler;

    @MockitoBean
    private CreateCategoryHandler createHandler;

    @MockitoBean
    private UpdateCategoryHandler updateHandler;

    @MockitoBean
    private DeleteCategoryHandler deleteHandler;

    @Test
    void returnsTheWholeTree() throws Exception {
        when(listHandler.handle(new ListCategoriesQuery()))
                .thenReturn(List.of(
                        new CategoryNode(
                                GAMES,
                                "Games",
                                List.of(new CategoryNode(
                                        ID, "Action", List.of(new CategoryNode(SHOOTERS, "Shooters", List.of()))))),
                        new CategoryNode(UUID.randomUUID(), "Demoscene", List.of())));

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(GAMES.toString()))
                .andExpect(jsonPath("$[0].title").value("Games"))
                .andExpect(jsonPath("$[0].children[0].title").value("Action"))
                .andExpect(jsonPath("$[0].children[0].children[0].id").value(SHOOTERS.toString()))
                .andExpect(jsonPath("$[0].children[0].children[0].children").isEmpty())
                .andExpect(jsonPath("$[1].title").value("Demoscene"));
    }

    @Test
    void returnsOneCategory() throws Exception {
        when(getHandler.handle(new GetCategoryQuery(new CategoryId(ID))))
                .thenReturn(new CategoryDetails(ID, "Action", GAMES));

        mvc.perform(get("/api/categories/{id}", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ID.toString()))
                .andExpect(jsonPath("$.title").value("Action"))
                .andExpect(jsonPath("$.parentId").value(GAMES.toString()));
    }

    @Test
    void returnsACategoryAtTheTopWithoutAParent() throws Exception {
        when(getHandler.handle(new GetCategoryQuery(new CategoryId(GAMES))))
                .thenReturn(new CategoryDetails(GAMES, "Games", null));

        mvc.perform(get("/api/categories/{id}", GAMES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.parentId").value(nullValue()));
    }

    @Test
    void answers404ForAnUnknownCategory() throws Exception {
        when(getHandler.handle(any())).thenThrow(new CategoryNotFoundException());

        mvc.perform(get("/api/categories/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("category.not-found"));
    }

    @Test
    void answers400ForAMalformedId() throws Exception {
        mvc.perform(get("/api/categories/{id}", "not-a-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void createsACategory() throws Exception {
        when(createHandler.handle(new CreateCategoryCommand(new CategoryTitle("Action"), new CategoryId(GAMES))))
                .thenReturn(new CategoryId(ID));

        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Action\",\"parentId\":\"" + GAMES + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/categories/" + ID))
                .andExpect(jsonPath("$.id").value(ID.toString()));
    }

    @Test
    void createsACategoryAtTheTop() throws Exception {
        when(createHandler.handle(new CreateCategoryCommand(new CategoryTitle("Series"), null)))
                .thenReturn(new CategoryId(ID));

        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Series\",\"parentId\":null}"))
                .andExpect(status().isCreated());
    }

    @Test
    void answers400ForACategoryWithoutATitle() throws Exception {
        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentId\":null}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.required"));
        verify(createHandler, never()).handle(any());
    }

    @Test
    void answers400ForATooLongTitle() throws Exception {
        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + "a".repeat(256) + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("title.too-long"));
    }

    @Test
    void answers400ForAMalformedParent() throws Exception {
        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Action\",\"parentId\":\"games\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void answers422ForAnUnknownParent() throws Exception {
        when(createHandler.handle(any())).thenThrow(new CategoryParentUnknownException());

        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Action\",\"parentId\":\"" + GAMES + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("category.parent-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("parentId"));
    }

    @Test
    void updatesACategory() throws Exception {
        mvc.perform(put("/api/categories/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Action games\",\"parentId\":\"" + GAMES + "\"}"))
                .andExpect(status().isNoContent());

        verify(updateHandler)
                .handle(new UpdateCategoryCommand(
                        new CategoryId(ID), new CategoryTitle("Action games"), new CategoryId(GAMES)));
    }

    @Test
    void answers422ForAParentInsideTheCategory() throws Exception {
        doThrow(new CategoryParentInsideException()).when(updateHandler).handle(any());

        mvc.perform(put("/api/categories/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Action\",\"parentId\":\"" + SHOOTERS + "\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("category.parent-inside"))
                .andExpect(jsonPath("$.errors[0].field").value("parentId"));
    }

    @Test
    void answers404WhenUpdatingAnUnknownCategory() throws Exception {
        doThrow(new CategoryNotFoundException()).when(updateHandler).handle(any());

        mvc.perform(put("/api/categories/{id}", ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Action\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("category.not-found"));
    }

    @Test
    void deletesACategory() throws Exception {
        mvc.perform(delete("/api/categories/{id}", ID)).andExpect(status().isNoContent());

        verify(deleteHandler).handle(new DeleteCategoryCommand(new CategoryId(ID)));
    }

    @Test
    void answers404WhenDeletingAnUnknownCategory() throws Exception {
        doThrow(new CategoryNotFoundException()).when(deleteHandler).handle(any());

        mvc.perform(delete("/api/categories/{id}", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("category.not-found"));
    }
}
