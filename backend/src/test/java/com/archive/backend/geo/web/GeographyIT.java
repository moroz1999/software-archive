package com.archive.backend.geo.web;

import static com.archive.backend.geo.GeoTestRows.city;
import static com.archive.backend.geo.GeoTestRows.country;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.session.AccessTokens;
import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Keeping the countries and the cities of the archive from the management (docs/domain/geo.md#keeping). */
@IntegrationTest
@Transactional
@Rollback
class GeographyIT {

    private static final UUID FINLAND = id(1);
    private static final UUID SWEDEN = id(2);
    private static final UUID HELSINKI = id(11);
    private static final UUID ESPOO = id(12);
    private static final UUID KOVROV = id(13);
    private static final UUID ABAKAN = id(14);
    private static final UUID JANE = id(21);
    private static final UUID IVAN = id(22);
    private static final UUID ELITE = id(31);
    private static final UUID TETRIS = id(32);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    /** Finland with Helsinki, where Jane Doe lives and of which she made Elite, and Espoo, where nobody is; Sweden. */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        country(jdbc, FINLAND, "Финляндия", "Finland");
        country(jdbc, SWEDEN, "Швеция", "Sweden");
        city(jdbc, HELSINKI, FINLAND, "Хельсинки", "Helsinki");
        city(jdbc, ESPOO, FINLAND, "Эспоо", null);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Jane Doe')", JANE);
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Ivan')", IVAN);
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id, city_id, is_primary)"
                        + " VALUES ('PERSON', ?, ?, ?, true)",
                JANE,
                FINLAND,
                HELSINKI);
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector')", ELITE);
        jdbc.update(
                "INSERT INTO software_contribution (software_id, person_id, role_codes) VALUES (?, ?, ARRAY['code'])",
                ELITE,
                JANE);
    }

    @Test
    void showsEveryCountryWithItsCitiesAndHowManyPlacesAreInThem() throws Exception {
        String finland = "$[?(@.id == '" + FINLAND + "')]";
        mvc.perform(get("/api/geography").header("Accept-Language", "en"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(finland + ".name").value(contains("Finland")))
                .andExpect(jsonPath(finland + ".places").value(contains(1)))
                .andExpect(jsonPath(finland + ".cities[*].name").value(contains("Helsinki", "Эспоо")))
                .andExpect(jsonPath(finland + ".cities[*].places").value(contains(1, 0)))
                .andExpect(jsonPath("$[?(@.id == '" + SWEDEN + "')].cities[*]").value(empty()));
    }

    @Test
    void addsACountryWithItsNamesAndCoordinates() throws Exception {
        String id = idOf(send(post("/api/countries"), ADMIN, """
                        {"names": {"ru": "Норвегия", "en": "Norway"}, "latitude": 60.5, "longitude": 8.5}
                        """).andExpect(status().isCreated()));

        mvc.perform(get("/api/countries/{id}", id))
                .andExpect(jsonPath("$.names.ru").value("Норвегия"))
                .andExpect(jsonPath("$.names.en").value("Norway"))
                .andExpect(jsonPath("$.latitude").value(60.5))
                .andExpect(jsonPath("$.longitude").value(8.5));
    }

    @Test
    void addsACityInACountryWithoutCoordinates() throws Exception {
        String id =
                idOf(send(post("/api/cities"), ADMIN, """
                        {"names": {"ru": "Турку"}, "countryId": "%s", "latitude": null, "longitude": null}
                        """.formatted(FINLAND)).andExpect(status().isCreated()));

        mvc.perform(get("/api/cities/{id}", id))
                .andExpect(jsonPath("$.names.ru").value("Турку"))
                .andExpect(jsonPath("$.names.en").doesNotExist())
                .andExpect(jsonPath("$.countryId").value(FINLAND.toString()))
                .andExpect(jsonPath("$.latitude").isEmpty());
        mvc.perform(get("/api/countries/{id}/cities", FINLAND).header("Accept-Language", "ru"))
                .andExpect(jsonPath("$[*].name").value(contains("Турку", "Хельсинки", "Эспоо")));
    }

    @Test
    void renamesACountryAndGivesItCoordinates() throws Exception {
        send(put("/api/countries/{id}", FINLAND), ADMIN, """
                        {"names": {"ru": "Суоми"}, "latitude": 64.0, "longitude": 26.0}
                        """).andExpect(status().isNoContent());

        mvc.perform(get("/api/countries/{id}", FINLAND))
                .andExpect(jsonPath("$.names.ru").value("Суоми"))
                .andExpect(jsonPath("$.names.en").doesNotExist())
                .andExpect(jsonPath("$.latitude").value(64.0));
    }

    @Test
    void movesThePlacesInACityMovedToAnotherCountryAndSendsTheirSoftwareToTheSearch() throws Exception {
        send(put("/api/cities/{id}", HELSINKI), ADMIN, """
                        {"names": {"ru": "Хельсинки", "en": "Helsinki"}, "countryId": "%s"}
                        """.formatted(SWEDEN)).andExpect(status().isNoContent());

        mvc.perform(get("/api/cities/{id}", HELSINKI))
                .andExpect(jsonPath("$.countryId").value(SWEDEN.toString()));
        mvc.perform(get("/api/authors/{id}", JANE))
                .andExpect(jsonPath("$.locations[0].countryId").value(SWEDEN.toString()))
                .andExpect(jsonPath("$.locations[0].cityId").value(HELSINKI.toString()));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM search_outbox WHERE entity_type = 'SOFTWARE' AND entity_id = ?",
                        Integer.class,
                        ELITE))
                .isOne();
    }

    @Test
    void deletesACountryAndACityNothingIsIn() throws Exception {
        send(delete("/api/cities/{id}", ESPOO), ADMIN, null).andExpect(status().isNoContent());
        send(delete("/api/countries/{id}", SWEDEN), ADMIN, null).andExpect(status().isNoContent());

        mvc.perform(get("/api/cities/{id}", ESPOO)).andExpect(status().isNotFound());
        mvc.perform(get("/api/countries/{id}", SWEDEN)).andExpect(status().isNotFound());
    }

    @Test
    void keepsACityPlacesAreInAndACountryWithCities() throws Exception {
        send(delete("/api/cities/{id}", HELSINKI), ADMIN, null)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("geo.city-in-use"));
        send(delete("/api/countries/{id}", FINLAND), ADMIN, null)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("geo.country-in-use"));

        mvc.perform(get("/api/cities/{id}", HELSINKI)).andExpect(status().isOk());
    }

    @Test
    void keepsACountryPlacesAreInDirectly() throws Exception {
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id, city_id, is_primary)"
                        + " VALUES ('PERSON', ?, ?, NULL, false)",
                JANE,
                SWEDEN);

        send(delete("/api/countries/{id}", SWEDEN), ADMIN, null)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("geo.country-in-use"));
    }

    @Test
    void letsOnlyAdministratorsKeepThePlaces() throws Exception {
        String body = """
                {"names": {"ru": "Норвегия"}}
                """;
        send(post("/api/countries"), MEMBER, body).andExpect(status().isForbidden());
        send(put("/api/countries/{id}", SWEDEN), MEMBER, body).andExpect(status().isForbidden());
        send(delete("/api/cities/{id}", ESPOO), MEMBER, null).andExpect(status().isForbidden());
        mvc.perform(post("/api/countries").contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refusesAPlaceWithoutARussianName() throws Exception {
        send(post("/api/countries"), ADMIN, """
                        {"names": {"en": "Norway"}}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("geo.name-required"));
        send(put("/api/cities/{id}", ESPOO), ADMIN, """
                        {"names": {"ru": "  "}, "countryId": "%s"}
                        """.formatted(FINLAND))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("geo.name-required"));
    }

    @Test
    void refusesCoordinatesOutOfTheGlobeOrHalfGiven() throws Exception {
        send(post("/api/countries"), ADMIN, """
                        {"names": {"ru": "Норвегия"}, "latitude": 91, "longitude": 8}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("geo.coordinates-invalid"));
        send(post("/api/countries"), ADMIN, """
                        {"names": {"ru": "Норвегия"}, "latitude": 60}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("geo.coordinates-invalid"));
    }

    @Test
    void refusesACityInACountryNotInTheArchive() throws Exception {
        send(post("/api/cities"), ADMIN, """
                        {"names": {"ru": "Осло"}, "countryId": "%s"}
                        """.formatted(id(99)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("geo.country-unknown"))
                .andExpect(jsonPath("$.errors[0].field").value("countryId"));
    }

    @Test
    void answersNotFoundForAPlaceNotInTheArchive() throws Exception {
        mvc.perform(get("/api/countries/{id}", id(99)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("geo.country-not-found"));
        mvc.perform(get("/api/cities/{id}", id(99)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("geo.city-not-found"));
        send(put("/api/cities/{id}", id(99)), ADMIN, """
                        {"names": {"ru": "Осло"}, "countryId": null}
                        """).andExpect(status().isNotFound());
        send(delete("/api/countries/{id}", id(99)), ADMIN, null).andExpect(status().isNotFound());
    }

    /** Kovrov, where Ivan lives and of which he made Tetris, and Abakan: two cities of no known country. */
    private void insertCitiesOfNoCountry() {
        city(jdbc, KOVROV, null, "Ковров", null);
        city(jdbc, ABAKAN, null, "Абакан", null);
        jdbc.update(
                "INSERT INTO entity_location (entity_type, entity_id, country_id, city_id, is_primary)"
                        + " VALUES ('PERSON', ?, NULL, ?, true)",
                IVAN,
                KOVROV);
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Tetris', 'vector')", TETRIS);
        jdbc.update(
                "INSERT INTO software_contribution (software_id, person_id, role_codes) VALUES (?, ?, ARRAY['code'])",
                TETRIS,
                IVAN);
    }

    @Test
    void showsTheCitiesOfNoCountryWithHowManyPlacesAreInThem() throws Exception {
        insertCitiesOfNoCountry();

        mvc.perform(get("/api/geography/cities-of-no-country").header("Accept-Language", "ru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(contains(ABAKAN.toString(), KOVROV.toString())))
                .andExpect(jsonPath("$[*].name").value(contains("Абакан", "Ковров")))
                .andExpect(jsonPath("$[*].places").value(contains(0, 1)));
    }

    @Test
    void putsACityOfNoCountryInACountryWithThePlacesInItAndSendsTheirSoftwareToTheSearch() throws Exception {
        insertCitiesOfNoCountry();

        send(put("/api/cities/{id}/country", KOVROV), ADMIN, """
                        {"countryId": "%s"}
                        """.formatted(FINLAND))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/cities/{id}", KOVROV))
                .andExpect(jsonPath("$.countryId").value(FINLAND.toString()));
        mvc.perform(get("/api/cities/{id}", KOVROV))
                .andExpect(jsonPath("$.names.ru").value("Ковров"));
        mvc.perform(get("/api/authors/{id}", IVAN))
                .andExpect(jsonPath("$.locations[0].countryId").value(FINLAND.toString()));
        mvc.perform(get("/api/geography/cities-of-no-country"))
                .andExpect(jsonPath("$[*].id").value(contains(ABAKAN.toString())));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM search_outbox WHERE entity_type = 'SOFTWARE' AND entity_id = ?",
                        Integer.class,
                        TETRIS))
                .isOne();
    }

    @Test
    void refusesPuttingACityInNoCountryOrInOneNotInTheArchive() throws Exception {
        insertCitiesOfNoCountry();

        send(put("/api/cities/{id}/country", KOVROV), ADMIN, """
                        {"countryId": null}
                        """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("geo.country-required"));
        send(put("/api/cities/{id}/country", KOVROV), ADMIN, """
                        {"countryId": "%s"}
                        """.formatted(id(99)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("geo.country-unknown"));
        send(put("/api/cities/{id}/country", id(99)), ADMIN, """
                        {"countryId": "%s"}
                        """.formatted(FINLAND))
                .andExpect(status().isNotFound());
        send(put("/api/cities/{id}/country", KOVROV), MEMBER, """
                        {"countryId": "%s"}
                        """.formatted(FINLAND))
                .andExpect(status().isForbidden());
    }

    private ResultActions send(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request,
            Actor actor,
            @Nullable String body)
            throws Exception {
        request.with(TestActors.bearer(accessTokens, actor));
        if (body != null) {
            request.contentType("application/json").content(body);
        }
        return mvc.perform(request);
    }

    private static String idOf(ResultActions created) throws Exception {
        return JsonPath.read(created.andReturn().getResponse().getContentAsString(), "$.id");
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-00ab-%012d".formatted(n));
    }
}
