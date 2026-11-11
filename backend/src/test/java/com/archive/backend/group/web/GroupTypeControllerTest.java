package com.archive.backend.group.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.group.app.ListGroupTypesHandler;
import com.archive.backend.group.app.ListGroupTypesQuery;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GroupTypeController.class)
@Import({SecurityConfig.class, WebConfig.class, ApiExceptionHandler.class})
class GroupTypeControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListGroupTypesHandler handler;

    @Test
    void listsTheGroupTypes() throws Exception {
        when(handler.handle(new ListGroupTypesQuery())).thenReturn(List.of("crack group", "demogroup"));

        mvc.perform(get("/api/group-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("crack group"))
                .andExpect(jsonPath("$[1]").value("demogroup"));
    }
}
