package com.archive.backend.hardware.web;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.session.AccessTokens;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

/** Keeping the hardware reference book and its groups from the management (docs/domain/hardware.md#hardware). */
@IntegrationTest
@Transactional
@Rollback
class HardwareEditingIT {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-00ac-000000000001");
    private static final UUID ELITE_DISK = UUID.fromString("00000000-0000-0000-00ac-000000000002");
    private static final UUID BOLDER = UUID.fromString("00000000-0000-0000-00ac-000000000003");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /** Elite for the Vector, made for the Vector-06C, with a release for the floppy disk. */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector')", ELITE);
        jdbc.update("INSERT INTO software_hardware (software_id, hardware_code) VALUES (?, 'vector06c')", ELITE);
        jdbc.update("INSERT INTO software_release (id, software_id) VALUES (?, ?)", ELITE_DISK, ELITE);
        jdbc.update(
                "INSERT INTO software_release_hardware (release_id, hardware_code) VALUES (?, 'floppy')", ELITE_DISK);
    }

    // Groups

    @Test
    void showsTheGroupsInTheirOrderWithTheirHardwareNamedInTheLanguageAskedFor() throws Exception {
        String computers = "$[?(@.code == 'computers')]";
        mvc.perform(get("/api/hardware-groups").header("Accept-Language", "ru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code")
                        .value(contains("computers", "storage", "dos", "sound", "controls", "expansion")))
                .andExpect(jsonPath("$[*].name")
                        .value(contains("Компьютеры", "Носители", "DOS", "Звук", "Управление", "Расширения")))
                .andExpect(jsonPath(computers + ".hardware[*].name")
                        .value(containsInAnyOrder("Вектор Старт-1200", "Вектор-06Ц", "Криста-2", "ПК-6128Ц")))
                .andExpect(jsonPath("$[?(@.code == 'dos')].hardware[*]").value(empty()));
        mvc.perform(get("/api/hardware-groups").header("Accept-Language", "en"))
                .andExpect(jsonPath("$[0].name").value("Computers"))
                .andExpect(jsonPath("$[0].hardware[0].name").value("Krista-2"));
    }

    @Test
    void addsAGroupAfterTheOthers() throws Exception {
        send(post("/api/hardware-groups"), ADMIN, """
                        {"code": "video", "names": {"ru": "Видео", "en": "Video"}}
                        """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/hardware-groups/video"))
                .andExpect(jsonPath("$.code").value("video"));

        mvc.perform(get("/api/hardware-groups/{code}", "video"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("video"))
                .andExpect(jsonPath("$.names.ru").value("Видео"))
                .andExpect(jsonPath("$.names.en").value("Video"));
        mvc.perform(get("/api/hardware-groups").header("Accept-Language", "en"))
                .andExpect(jsonPath("$[6].code").value("video"))
                .andExpect(jsonPath("$[6].name").value("Video"))
                .andExpect(jsonPath("$[6].hardware").value(empty()));
    }

    @Test
    void renamesAGroup() throws Exception {
        send(put("/api/hardware-groups/{code}", "storage"), ADMIN, """
                        {"names": {"ru": "Накопители"}}
                        """).andExpect(status().isNoContent());

        mvc.perform(get("/api/hardware-groups/{code}", "storage"))
                .andExpect(jsonPath("$.names.ru").value("Накопители"))
                .andExpect(jsonPath("$.names.en").doesNotExist());
    }

    @Test
    void refusesAGroupWithoutARussianNameWithABadCodeOrWithACodeTaken() throws Exception {
        send(post("/api/hardware-groups"), ADMIN, """
                        {"code": "video", "names": {"en": "Video"}}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("hardware.name-required"));
        send(post("/api/hardware-groups"), ADMIN, """
                        {"code": "Video Cards", "names": {"ru": "Видео"}}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("hardware.group-code-invalid"));
        send(post("/api/hardware-groups"), ADMIN, """
                        {"code": "sound", "names": {"ru": "Звук ещё раз"}}
                        """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("hardware.group-code-taken"))
                .andExpect(jsonPath("$.errors[0].field").value("code"));

        mvc.perform(get("/api/hardware-groups/{code}", "sound"))
                .andExpect(jsonPath("$.names.ru").value("Звук"));
    }

    @Test
    void deletesAnEmptyGroupAndKeepsOneWithHardware() throws Exception {
        send(delete("/api/hardware-groups/{code}", "dos"), ADMIN, null).andExpect(status().isNoContent());
        mvc.perform(get("/api/hardware-groups/{code}", "dos"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("hardware.group-not-found"));

        send(delete("/api/hardware-groups/{code}", "sound"), ADMIN, null)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.group-in-use"));
        mvc.perform(get("/api/hardware-groups/{code}", "sound")).andExpect(status().isOk());
    }

    @Test
    void answersNotFoundForAGroupNotInTheArchive() throws Exception {
        mvc.perform(get("/api/hardware-groups/{code}", "video"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("hardware.group-not-found"));
        send(put("/api/hardware-groups/{code}", "video"), ADMIN, """
                        {"names": {"ru": "Видео"}}
                        """).andExpect(status().isNotFound());
        send(delete("/api/hardware-groups/{code}", "video"), ADMIN, null).andExpect(status().isNotFound());
    }

    @Test
    void letsOnlyAnAdministratorKeepTheGroups() throws Exception {
        String body = """
                {"code": "video", "names": {"ru": "Видео"}}
                """;
        send(post("/api/hardware-groups"), MEMBER, body).andExpect(status().isForbidden());
        mvc.perform(post("/api/hardware-groups").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
        send(put("/api/hardware-groups/{code}", "dos"), MEMBER, body).andExpect(status().isForbidden());
        send(delete("/api/hardware-groups/{code}", "dos"), MEMBER, null).andExpect(status().isForbidden());

        mvc.perform(get("/api/hardware-groups/{code}", "dos")).andExpect(status().isOk());
        mvc.perform(get("/api/hardware-groups/{code}", "video")).andExpect(status().isNotFound());
    }

    // Hardware

    @Test
    void addsHardwareToAGroupAndPlatforms() throws Exception {
        send(post("/api/hardware"), ADMIN, """
                        {"code": "ay-turbo", "names": {"ru": "Турбо-AY", "en": "Turbo AY"}, "group": "sound",
                         "platforms": ["vector", "specialist"]}
                        """)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/hardware/ay-turbo"))
                .andExpect(jsonPath("$.code").value("ay-turbo"));

        mvc.perform(get("/api/hardware/{code}", "ay-turbo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ay-turbo"))
                .andExpect(jsonPath("$.names.ru").value("Турбо-AY"))
                .andExpect(jsonPath("$.names.en").value("Turbo AY"))
                .andExpect(jsonPath("$.group").value("sound"))
                .andExpect(jsonPath("$.platforms").value(contains("specialist", "vector")));
        mvc.perform(get("/api/hardware").param("platform", "specialist").header("Accept-Language", "en"))
                .andExpect(jsonPath("$[*].code").value(contains("ay-turbo")))
                .andExpect(jsonPath("$[0].groupName").value("Sound"));
    }

    @Test
    void showsHardwareOfANewGroupAfterTheOthersWithTheNameOfItsGroup() throws Exception {
        send(post("/api/hardware-groups"), ADMIN, """
                        {"code": "video", "names": {"ru": "Видео", "en": "Video"}}
                        """).andExpect(status().isCreated());
        send(post("/api/hardware"), ADMIN, """
                        {"code": "gpu", "names": {"ru": "Видеоускоритель"}, "group": "video", "platforms": ["vector"]}
                        """).andExpect(status().isCreated());
        jdbc.update("INSERT INTO software_hardware (software_id, hardware_code) VALUES (?, 'gpu')", ELITE);

        mvc.perform(get("/api/hardware").param("platform", "vector").header("Accept-Language", "ru"))
                .andExpect(jsonPath("$[-1].code").value("gpu"))
                .andExpect(jsonPath("$[-1].group").value("video"))
                .andExpect(jsonPath("$[-1].groupName").value("Видео"))
                .andExpect(jsonPath("$[0].groupName").value("Компьютеры"));
        mvc.perform(get("/api/software/{id}", ELITE).header("Accept-Language", "en"))
                .andExpect(jsonPath("$.hardware[*].code").value(contains("vector06c", "gpu")))
                .andExpect(jsonPath("$.hardware[*].groupName").value(contains("Computers", "Video")));
        mvc.perform(get("/api/hardware-groups").header("Accept-Language", "en"))
                .andExpect(jsonPath("$[?(@.code == 'video')].hardware[*].name").value(contains("Видеоускоритель")));
    }

    @Test
    void editsTheNamesTheGroupAndThePlatformsOfHardwareAndSendsItsSoftwareToTheSearch() throws Exception {
        send(put("/api/hardware/{code}", "floppy"), ADMIN, """
                        {"names": {"ru": "Гибкий диск"}, "group": "expansion", "platforms": ["vector", "bk"]}
                        """).andExpect(status().isNoContent());

        mvc.perform(get("/api/hardware/{code}", "floppy"))
                .andExpect(jsonPath("$.names.ru").value("Гибкий диск"))
                .andExpect(jsonPath("$.names.en").doesNotExist())
                .andExpect(jsonPath("$.group").value("expansion"))
                .andExpect(jsonPath("$.platforms").value(contains("bk", "vector")));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM search_outbox WHERE entity_type = 'SOFTWARE' AND entity_id = ?",
                        Integer.class,
                        ELITE))
                .isOne();
    }

    @Test
    void refusesHardwareWithoutARussianNameWithABadCodeOrWithoutPlatforms() throws Exception {
        send(post("/api/hardware"), ADMIN, """
                        {"code": "gpu", "names": {"en": "GPU"}, "group": "sound", "platforms": ["vector"]}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("hardware.name-required"));
        send(post("/api/hardware"), ADMIN, """
                        {"code": "GPU 1", "names": {"ru": "GPU"}, "group": "sound", "platforms": ["vector"]}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("hardware.code-invalid"));
        send(post("/api/hardware"), ADMIN, """
                        {"code": "gpu", "names": {"ru": "GPU"}, "group": "sound", "platforms": []}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("hardware.platform-required"));
        send(post("/api/hardware"), ADMIN, """
                        {"code": "gpu", "names": {"ru": "%s"}, "group": "sound", "platforms": ["vector"]}
                        """.formatted("Я".repeat(256)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("hardware.name-too-long"));

        mvc.perform(get("/api/hardware/{code}", "gpu")).andExpect(status().isNotFound());
    }

    @Test
    void refusesHardwareWithACodeTakenAGroupOrAPlatformNotInTheArchive() throws Exception {
        send(post("/api/hardware"), ADMIN, """
                        {"code": "floppy", "names": {"ru": "Дискета"}, "group": "storage", "platforms": ["vector"]}
                        """)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("hardware.code-taken"))
                .andExpect(jsonPath("$.errors[0].field").value("code"));
        send(post("/api/hardware"), ADMIN, """
                        {"code": "gpu", "names": {"ru": "GPU"}, "group": "video", "platforms": ["vector"]}
                        """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.group-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("group"));
        send(post("/api/hardware"), ADMIN, """
                        {"code": "gpu", "names": {"ru": "GPU"}, "group": "sound", "platforms": ["zx"]}
                        """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.platform-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("platforms"));
        send(put("/api/hardware/{code}", "floppy"), ADMIN, """
                        {"names": {"ru": "Дискета"}, "group": "video", "platforms": ["vector"]}
                        """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.group-unknown"));

        mvc.perform(get("/api/hardware/{code}", "gpu")).andExpect(status().isNotFound());
        mvc.perform(get("/api/hardware/{code}", "floppy"))
                .andExpect(jsonPath("$.group").value("storage"));
    }

    @Test
    void keepsThePlatformOfHardwareItsSoftwareOrItsReleasesHave() throws Exception {
        send(put("/api/hardware/{code}", "vector06c"), ADMIN, """
                        {"names": {"ru": "Вектор-06Ц"}, "group": "computers", "platforms": ["bk"]}
                        """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.platform-in-use"))
                .andExpect(jsonPath("$.errors[0].field").value("platforms"));
        send(put("/api/hardware/{code}", "floppy"), ADMIN, """
                        {"names": {"ru": "Дискета"}, "group": "storage", "platforms": ["bk"]}
                        """)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.platform-in-use"));

        mvc.perform(get("/api/hardware/{code}", "vector06c"))
                .andExpect(jsonPath("$.platforms").value(contains("vector")));
        mvc.perform(get("/api/hardware/{code}", "floppy"))
                .andExpect(jsonPath("$.platforms").value(contains("vector")));
    }

    @Test
    void takesAPlatformNoSoftwareOfItHasTheHardwareOn() throws Exception {
        jdbc.update("INSERT INTO hardware_platform (hardware_code, platform_code) VALUES ('ay8910', 'bk')");
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Bolder Dash', 'bk')", BOLDER);

        send(put("/api/hardware/{code}", "ay8910"), ADMIN, """
                        {"names": {"ru": "AY-3-8910"}, "group": "sound", "platforms": ["vector"]}
                        """).andExpect(status().isNoContent());

        mvc.perform(get("/api/hardware/{code}", "ay8910"))
                .andExpect(jsonPath("$.platforms").value(contains("vector")));
    }

    @Test
    void deletesHardwareNothingHasAndKeepsHardwareOfSoftwareOrOfARelease() throws Exception {
        send(delete("/api/hardware/{code}", "z80"), ADMIN, null).andExpect(status().isNoContent());
        mvc.perform(get("/api/hardware/{code}", "z80"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("hardware.not-found"));

        send(delete("/api/hardware/{code}", "vector06c"), ADMIN, null)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.in-use"));
        send(delete("/api/hardware/{code}", "floppy"), ADMIN, null)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("hardware.in-use"));
        mvc.perform(get("/api/hardware/{code}", "vector06c")).andExpect(status().isOk());
        mvc.perform(get("/api/hardware/{code}", "floppy")).andExpect(status().isOk());
    }

    @Test
    void answersNotFoundForHardwareNotInTheArchive() throws Exception {
        mvc.perform(get("/api/hardware/{code}", "gpu"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("hardware.not-found"));
        send(put("/api/hardware/{code}", "gpu"), ADMIN, """
                        {"names": {"ru": "GPU"}, "group": "sound", "platforms": ["vector"]}
                        """).andExpect(status().isNotFound());
        send(delete("/api/hardware/{code}", "gpu"), ADMIN, null).andExpect(status().isNotFound());
    }

    @Test
    void letsOnlyAnAdministratorKeepTheHardware() throws Exception {
        String body = """
                {"code": "gpu", "names": {"ru": "GPU"}, "group": "sound", "platforms": ["vector"]}
                """;
        send(post("/api/hardware"), MEMBER, body).andExpect(status().isForbidden());
        mvc.perform(post("/api/hardware").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
        send(put("/api/hardware/{code}", "z80"), MEMBER, body).andExpect(status().isForbidden());
        send(delete("/api/hardware/{code}", "z80"), MEMBER, null).andExpect(status().isForbidden());

        mvc.perform(get("/api/hardware/{code}", "z80"))
                .andExpect(jsonPath("$.group").value("expansion"));
        mvc.perform(get("/api/hardware/{code}", "gpu")).andExpect(status().isNotFound());
    }

    private ResultActions send(MockHttpServletRequestBuilder request, Actor actor, @Nullable String body)
            throws Exception {
        request.with(TestActors.bearer(accessTokens, actor));
        if (body != null) {
            request.contentType("application/json").content(body);
        }
        return mvc.perform(request);
    }
}
