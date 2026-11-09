package com.archive.backend.hardware.web;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.hardware.app.ListPlatformsHandler;
import com.archive.backend.hardware.app.ListPlatformsQuery;
import com.archive.backend.hardware.read.PlatformItem;
import com.archive.backend.hardware.web.mapper.PlatformWebMapperImpl;
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

@WebMvcTest(PlatformController.class)
@Import({SecurityConfig.class, WebConfig.class, PlatformWebMapperImpl.class, ApiExceptionHandler.class})
class PlatformControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListPlatformsHandler handler;

    @Test
    void listsThePlatforms() throws Exception {
        when(handler.handle(new ListPlatformsQuery())).thenReturn(List.of(new PlatformItem("vector", "Vector")));

        mvc.perform(get("/api/platforms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("vector"))
                .andExpect(jsonPath("$[0].name").value("Vector"));
    }
}
