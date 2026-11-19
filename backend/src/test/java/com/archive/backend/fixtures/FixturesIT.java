package com.archive.backend.fixtures;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.TypesenseTestConfiguration;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * The "fixtures" profile seeds the database with the e2e data set on startup.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("fixtures")
@Import({TestcontainersConfiguration.class, TypesenseTestConfiguration.class})
class FixturesIT {

    /** A fixture country with its 20 cities. */
    private static final String FINLAND = "00000000-0000-0000-0002-000000000006";
    /** A fixture software with base hardware, a release of its own sound and a release without hardware. */
    private static final String ELITE = "00000000-0000-0000-0001-000000000001";
    /** A fixture RK game released for every machine of the family, with music on the Apogey. */
    private static final String KHRYUKOSTYAG = "00000000-0000-0000-0001-000000000015";
    /** A fixture group with every kind of member and groups around it. */
    private static final String BYTE_BRIGADE = "00000000-0000-0000-0005-000000000001";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void everyFixtureEntryIsAddedByTheFixtureAdministrator() {
        for (String table : List.of("software", "software_release", "person", "\"group\"")) {
            List<Object> adders = jdbc.queryForList(
                    "SELECT DISTINCT added_by FROM " + table + " WHERE id::text LIKE '00000000-0000-0000-000%'",
                    Object.class);
            assertThat(adders)
                    .as(table)
                    .extracting(String::valueOf)
                    .containsExactly("00000000-0000-0000-0009-000000000001");
        }
    }

    @Test
    void everyFixtureLabelIsAddedByAFixtureUserTheLabelsOfNobodyByTheMember() {
        assertThat(jdbc.queryForList(
                        "SELECT DISTINCT added_by::text FROM label WHERE id::text LIKE '00000000-0000-0000-0004-%' AND person_id IS NULL AND group_id IS NULL",
                        String.class))
                .containsExactly("00000000-0000-0000-0009-000000000003");
        assertThat(jdbc.queryForList(
                        "SELECT DISTINCT added_by::text FROM label WHERE id::text LIKE '00000000-0000-0000-0004-%' AND (person_id IS NOT NULL OR group_id IS NOT NULL)",
                        String.class))
                .containsExactly("00000000-0000-0000-0009-000000000001");
    }

    @Test
    void fixtureSoftwareIsServedByTheApi() throws Exception {
        mvc.perform(get("/api/software").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].title").value(hasItems("Elite", "Manic Miner", "Knight Lore")));
    }

    @Test
    void fixtureReleaseFileStructureIsServedByTheApi() throws Exception {
        mvc.perform(get(
                        "/api/software/{softwareId}/releases/{releaseId}/files",
                        ELITE,
                        "00000000-0000-0000-0007-000000000002"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.root.name").value("elite-ay.txt"))
                .andExpect(jsonPath("$.root.directory").value(false))
                .andExpect(jsonPath("$.root.executable").value(false))
                .andExpect(jsonPath("$.root.children", hasSize(0)));
    }

    @Test
    void fixtureScreenshotsAreServedWithTheirPictures() throws Exception {
        mvc.perform(get("/api/software/{id}/screenshots", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value("00000000-0000-0000-000a-000000000001"));
        mvc.perform(get("/api/screenshots/{id}", "00000000-0000-0000-000a-000000000001"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "image/png"));
        mvc.perform(get("/api/screenshots/{id}", "00000000-0000-0000-000a-000000000006"))
                .andExpect(status().isOk());
    }

    @Test
    void theCardOfFixtureSoftwareWithoutScreenshotsOfItsOwnShowsThoseOfItsRelease() throws Exception {
        mvc.perform(get("/api/software/latest").param("platform", "bk").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '00000000-0000-0000-0001-000000000004')].screenshots[*].id")
                        .value(contains(
                                "00000000-0000-0000-000a-000000000005", "00000000-0000-0000-000a-000000000006")));
    }

    @Test
    void fixtureCountriesAndCitiesAreServedByTheApi() throws Exception {
        // The 20 countries of the fixtures and the country "Unknown" of the schema.
        mvc.perform(get("/api/countries")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(21)));
        mvc.perform(get("/api/countries/{id}/cities", FINLAND))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(20)))
                .andExpect(jsonPath("$[*].name").value(hasItems("Helsinki", "Tampere")));

        mvc.perform(get("/api/countries/{id}/cities", FINLAND).header("Accept-Language", "ru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name").value(hasItems("Хельсинки", "Тампере")));
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
                .andExpect(jsonPath("$.content[?(@.note == null && @.personId != null)].name")
                        .value(contains("Ivan Petrov")));
    }

    @Test
    void fixtureLabelsOfNobodyAreInTheAuthorCatalog() throws Exception {
        mvc.perform(get("/api/authors").param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.labelId != null && @.note == null)].name")
                        .value(containsInAnyOrder("Ghost Rider", "Mystery Coder", "Аноним")));
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
                .andExpect(jsonPath("$.name").value("Byte Brigade"))
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
    void everyPlatformHasSeveralFixtureSoftware() throws Exception {
        List<String> platforms = JsonPath.read(body(get("/api/platforms")), "$[*].code");

        assertThat(platforms)
                .containsExactlyInAnyOrder(
                        "vector",
                        "bk",
                        "agat",
                        "msx",
                        "specialist",
                        "rk",
                        "orion",
                        "pk8000",
                        "korvet",
                        "lviv",
                        "okean",
                        "bashkiria",
                        "xt");
        for (String platform : platforms) {
            int count = JsonPath.read(body(get("/api/software").param("platform", platform)), "$.page.totalElements");

            assertThat(count).as(platform).isGreaterThanOrEqualTo(4);
        }
    }

    @Test
    void noPlatformButTheVectorFamilyHasHardwareYet() throws Exception {
        for (String platform : new String[] {
            "agat", "bashkiria", "bk", "korvet", "lviv", "msx", "okean", "orion", "pk8000", "rk", "specialist", "xt"
        }) {
            mvc.perform(get("/api/hardware").param("platform", platform))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Test
    void aFixtureRkGameHasAReleaseForEveryMachine() throws Exception {
        List<Map<String, Object>> releases = JsonPath.read(body(get("/api/software/{id}/releases", KHRYUKOSTYAG)), "$");

        assertThat(releases).hasSize(3);
    }

    @Test
    void aFixtureSoftwareHasItsPlatformAndHardwareOfSeveralGroups() throws Exception {
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.platform.code").value("vector"))
                .andExpect(jsonPath("$.hardware[*].group").value(hasItems("computers", "storage")))
                .andExpect(jsonPath("$.hardware[*].code").value(hasItem("tape")));
    }

    @Test
    void aFixtureSoftwareIsInTwoCategoriesOfTheTree() throws Exception {
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[*].title").value(containsInAnyOrder("Strategy", "Shooters")));
    }

    @Test
    void aFixtureSoftwareCreditsPeopleGroupsAndTheirLabelsAsPublishersAndDevelopers() throws Exception {
        mvc.perform(get("/api/software/{id}", ELITE))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.credits[*].type").value(hasItems("author", "author-label", "group", "group-label")))
                .andExpect(jsonPath("$.credits[*].roles[*]").value(hasItems("publisher", "developer")));
    }

    @Test
    void aFixtureSoftwareHasAReleaseWithItsOwnSoundAndOneShowingTheHardwareOfTheSoftware() throws Exception {
        List<Map<String, Object>> releases = JsonPath.read(body(get("/api/software/{id}/releases", ELITE)), "$");

        assertThat(releases)
                .anySatisfy(release -> assertThat(hardwareCodes(release)).contains("vector06c", "tape", "ay8910"))
                .anySatisfy(release -> assertThat(hardwareCodes(release))
                        .contains("vector06c", "tape")
                        .doesNotContain("ay8910"));
        assertThat(releases).extracting(release -> release.get("type")).contains("original", "mod");
    }

    @Test
    void theHardwareOfAPlatformIsServedToChooseFrom() throws Exception {
        mvc.perform(get("/api/hardware").param("platform", "vector"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].code")
                        .value(containsInAnyOrder("vector06c", "krista2", "tape", "floppy", "ay8910")));
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
