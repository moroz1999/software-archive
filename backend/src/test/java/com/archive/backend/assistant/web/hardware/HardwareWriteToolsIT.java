package com.archive.backend.assistant.web.hardware;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolJson;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.BusinessException;
import com.archive.backend.user.TestActors;
import com.jayway.jsonpath.DocumentContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/** New hardware a plan adds: kept by administrators, by the rules of the hardware management. */
@IntegrationTest
@Transactional
@Rollback
class HardwareWriteToolsIT {

    private static final ToolContext ADMIN = new ToolContext(TestActors.ADMIN, Language.EN);
    private static final ToolContext MEMBER = new ToolContext(TestActors.MEMBER, Language.EN);
    private static final String COVOX = """
            {"code": "covox", "names": {"ru": "Ковокс", "en": "Covox"}, "group": "sound", "platforms": ["vector"]}
            """;

    @Autowired
    private ToolCalls calls;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
    }

    @Test
    void addsHardwareOfAPlatformToAGroup() {
        assertThat(calls.state("create_hardware", COVOX, ADMIN)).isNull();

        String code = calls.write("create_hardware", COVOX, ADMIN).id();

        assertThat(code).isEqualTo("covox");
        DocumentContext hardware = ToolJson.of(calls.read("list_hardware", "{\"platform\": \"vector\"}", ADMIN));
        assertThat(ToolJson.list(hardware, "$[?(@.code == 'covox')].name")).containsExactly("Covox");
        assertThat(ToolJson.list(hardware, "$[?(@.code == 'covox')].group")).containsExactly("sound");
    }

    @Test
    void refusesACodeTakenAndLeavesKeepingHardwareToAdministrators() {
        assertThatThrownBy(() -> calls.write("create_hardware", COVOX.replace("covox", "tape"), ADMIN))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calls.write("create_hardware", COVOX, MEMBER)).isInstanceOf(BusinessException.class);
    }
}
