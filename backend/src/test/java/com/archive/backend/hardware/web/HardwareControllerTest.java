package com.archive.backend.hardware.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.hardware.app.ListHardwareHandler;
import com.archive.backend.hardware.app.ListHardwareQuery;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.hardware.read.HardwareItem;
import com.archive.backend.hardware.web.mapper.HardwareWebMapperImpl;
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

@WebMvcTest(HardwareController.class)
@Import({SecurityConfig.class, WebConfig.class, HardwareWebMapperImpl.class, ApiExceptionHandler.class})
class HardwareControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListHardwareHandler handler;

    @Test
    void listsTheHardwareOfAPlatform() throws Exception {
        when(handler.handle(new ListHardwareQuery(new PlatformCode("vector"))))
                .thenReturn(List.of(new HardwareItem("ay8910", "AY-3-8910", "sound")));

        mvc.perform(get("/api/hardware").param("platform", "vector"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("ay8910"))
                .andExpect(jsonPath("$[0].name").value("AY-3-8910"))
                .andExpect(jsonPath("$[0].group").value("sound"));
    }

    @Test
    void answers400WithoutAPlatform() throws Exception {
        mvc.perform(get("/api/hardware"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.required"));
        verify(handler, never()).handle(any());
    }

    @Test
    void answers400ForAnInvalidPlatform() throws Exception {
        mvc.perform(get("/api/hardware").param("platform", "BK!"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("platform.code-invalid"));
    }
}
