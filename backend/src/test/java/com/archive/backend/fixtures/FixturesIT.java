package com.archive.backend.fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.TestcontainersConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * The "fixtures" profile seeds the database with the e2e data set on startup.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("fixtures")
@Import(TestcontainersConfiguration.class)
class FixturesIT {

    /** A fixture country with its 20 cities. */
    private static final String FINLAND = "00000000-0000-0000-0002-000000000006";
    /** A fixture software with base hardware, a release of its own sound and a release without hardware. */
    private static final String ELITE = "00000000-0000-0000-0001-000000000001";
    /** A fixture group with every kind of member and groups around it. */
    private static final String BYTE_BRIGADE = "00000000-0000-0000-0005-000000000001";

    @Autowired
    private MockMvc mvc;

    @Test
    void fixtureSoftwareIsServedByTheApi() throws Exception {
        mvc.perform(get("/api/software"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title").value(hasItems("Elite", "Manic Miner", "Knight Lore")));
    }

    @Test
    void fixtureCountriesAndCitiesAreServedByTheApi() throws Exception {
        mvc.perform(get("/api/countries")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(20)));
        mvc.perform(get("/api/countries/{id}/cities", FINLAND))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[*].name").value(hasItems("Helsinki", "Tampere")));
    }

    @Test
    void fixtureAuthorsFillMoreThanOneCatalogPage() throws Exception {
        mvc.perform(get("/api/authors").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(100)))
                .andExpect(jsonPath("$.page.totalPages").value(greaterThan(1)));
    }

    @Test
    void everyFixtureAuthorHasARealNameButTheOneKnownOnlyByIt() throws Exception {
        mvc.perform(get("/api/authors").param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.realName == null)].name").value(contains("Ivan Petrov")));
    }

    @Test
    void fixtureGroupsFillMoreThanOneCatalogPage() throws Exception {
        mvc.perform(get("/api/groups").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(100)))
                .andExpect(jsonPath("$.page.totalPages").value(greaterThan(1)));
    }

    @Test
    void aFixtureGroupHasMembersUnderFormerLabelsUnknownMembersAndGroupsAroundIt() throws Exception {
        mvc.perform(get("/api/groups/{id}", BYTE_BRIGADE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(greaterThan(1))))
                .andExpect(jsonPath("$.members[?(@.personName != null)]", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.members[?(@.personId == null)]", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.memberGroups", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.groups", hasSize(greaterThan(0))));
    }

    @Test
    void fixtureAuthorsCoverLatinCyrillicAndTheRestLetters() throws Exception {
        for (String letter : new String[] {"A", "Б", "#"}) {
            mvc.perform(get("/api/authors").param("letter", letter))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
        }
    }

    @Test
    void everyPlatformHasFixtureSoftware() throws Exception {
        for (String platform : new String[] {"vector", "bk", "agat", "msx", "specialist"}) {
            mvc.perform(get("/api/software").param("platform", platform))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
        }
    }

    @Test
    void aFixtureSoftwareHasItsPlatformAndHardwareOfSeveralGroups() throws Exception {
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform.code").value("vector"))
                .andExpect(jsonPath("$.hardware[*].group").value(hasItems("computers", "sound")))
                .andExpect(jsonPath("$.hardware[*].code").value(hasItem("beeper")));
    }

    @Test
    void aFixtureSoftwareHasAReleaseWithItsOwnSoundAndOneShowingTheHardwareOfTheSoftware() throws Exception {
        List<Map<String, Object>> releases = JsonPath.read(body(get("/api/software/{id}/releases", ELITE)), "$");

        assertThat(releases)
                .anySatisfy(release -> assertThat(hardwareCodes(release))
                        .contains("vector06c", "ay8910")
                        .doesNotContain("beeper"))
                .anySatisfy(release -> assertThat(hardwareCodes(release)).contains("vector06c", "beeper"));
        assertThat(releases).extracting(release -> release.get("type")).contains("original", "mod");
    }

    @Test
    void thePlatformKeepsOnlySomeOfTheFixtureAuthorsAndGroups() throws Exception {
        for (String catalog : new String[] {"/api/authors", "/api/groups"}) {
            int all = JsonPath.read(body(get(catalog)), "$.page.totalElements");
            int bk = JsonPath.read(body(get(catalog).param("platform", "bk")), "$.page.totalElements");

            assertThat(bk).as(catalog).isPositive().isLessThan(all);
        }
    }

    private String body(MockHttpServletRequestBuilder request) throws Exception {
        return mvc.perform(request)
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    @SuppressWarnings("unchecked")
    private static List<String> hardwareCodes(Map<String, Object> release) {
        return ((List<Map<String, Object>>) Objects.requireNonNull(release.get("hardware")))
                .stream().map(item -> (String) item.get("code")).toList();
    }
}
