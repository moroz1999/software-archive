package com.archive.backend.assistant.web.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.assistant.web.tool.ToolArgumentsException;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolJson;
import com.archive.backend.geo.GeoTestRows;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.BusinessException;
import com.archive.backend.user.TestActors;
import com.jayway.jsonpath.DocumentContext;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/** Finding countries and cities by name, and the changes of them a plan makes: kept by administrators. */
@IntegrationTest
@Transactional
@Rollback
class PlaceToolsIT {

    private static final UUID FINLAND = id(1);
    private static final UUID RUSSIA = id(2);
    private static final UUID HELSINKI = id(11);
    private static final UUID NEW_HELSINKI = id(12);
    private static final UUID PETERSBURG = id(13);
    private static final UUID YOLKINO = id(14);
    private static final UUID TALLINN = id(15);

    private static final ToolContext ADMIN = new ToolContext(TestActors.ADMIN, Language.EN);
    private static final ToolContext MEMBER = new ToolContext(TestActors.MEMBER, Language.EN);

    @Autowired
    private ToolCalls calls;

    @Autowired
    private JdbcTemplate jdbc;

    /**
     * Finland with Helsinki and New Helsinki (Новые Хельсинки, no English name); Russia with Saint Petersburg and
     * Ёлкино; Tallinn, a city of no known country.
     */
    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        GeoTestRows.country(jdbc, FINLAND, "Финляндия", "Finland");
        GeoTestRows.country(jdbc, RUSSIA, "Россия", "Russia");
        GeoTestRows.city(jdbc, HELSINKI, FINLAND, "Хельсинки", "Helsinki");
        GeoTestRows.city(jdbc, NEW_HELSINKI, FINLAND, "Новые Хельсинки", null);
        GeoTestRows.city(jdbc, PETERSBURG, RUSSIA, "Санкт-Петербург", "Saint Petersburg");
        GeoTestRows.city(jdbc, YOLKINO, RUSSIA, "Ёлкино", null);
        GeoTestRows.city(jdbc, TALLINN, null, "Таллин", "Tallinn");
    }

    @Test
    void findsCitiesByANameInAnyLanguageWithTheirCountry() {
        DocumentContext found = find("хельсинки");

        assertThat(ToolJson.list(found, "$[*].id")).containsExactly(HELSINKI.toString(), NEW_HELSINKI.toString());
        assertThat(found.read("$[0].kind", String.class)).isEqualTo("city");
        assertThat(found.read("$[0].name", String.class)).isEqualTo("Helsinki");
        assertThat(found.read("$[0].names.ru", String.class)).isEqualTo("Хельсинки");
        assertThat(found.read("$[0].countryId", String.class)).isEqualTo(FINLAND.toString());
        assertThat(found.read("$[0].country", String.class)).isEqualTo("Finland");
        assertThat(ToolJson.list(find("Helsinki"), "$[*].id")).containsExactly(HELSINKI.toString());
    }

    @Test
    void findsCountriesAndCitiesOfNoKnownCountry() {
        DocumentContext country = find("FINLAND");
        DocumentContext city = find("tallinn");

        assertThat(ToolJson.list(country, "$[*].id")).containsExactly(FINLAND.toString());
        assertThat(country.read("$[0].kind", String.class)).isEqualTo("country");
        assertThat(ToolJson.list(city, "$[*].id")).containsExactly(TALLINN.toString());
        assertThat(city.read("$[0].countryId", String.class)).isNull();
    }

    @Test
    void findsANameRegardlessOfSpacesHyphensAndYo() {
        assertThat(ToolJson.list(find("санкт петербург"), "$[*].id")).containsExactly(PETERSBURG.toString());
        assertThat(ToolJson.list(find("Saint-Petersburg"), "$[*].id")).containsExactly(PETERSBURG.toString());
        assertThat(ToolJson.list(find("елкино"), "$[*].id")).containsExactly(YOLKINO.toString());
        assertThat(ToolJson.list(find("Атлантида"), "$")).isEmpty();
    }

    @Test
    void refusesToLookForNoName() {
        assertThatThrownBy(() -> calls.read("find_places", "{\"name\": \"\"}", ADMIN))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("name");
    }

    @Test
    void addsACountryWithItsNames() {
        String estonia = "{\"names\": {\"ru\": \"Эстония\", \"en\": \"Estonia\"}}";
        assertThat(calls.state("create_country", estonia, ADMIN)).isNull();

        UUID id = calls.write("create_country", estonia, ADMIN).uuid();

        DocumentContext found = find("Estonia");
        assertThat(ToolJson.list(found, "$[*].id")).containsExactly(id.toString());
        assertThat(found.read("$[0].names.ru", String.class)).isEqualTo("Эстония");
    }

    @Test
    void addsACityInACountryOrOfNoKnownCountry() {
        UUID turku = calls.write("create_city", """
                        {"names": {"ru": "Турку", "en": "Turku"}, "countryId": "%s"}
                        """.formatted(FINLAND), ADMIN).uuid();
        UUID somewhere = calls.write("create_city", "{\"names\": {\"ru\": \"Нигдеград\"}}", ADMIN)
                .uuid();

        assertThat(find("Turku").read("$[0].id", String.class)).isEqualTo(turku.toString());
        assertThat(find("Turku").read("$[0].countryId", String.class)).isEqualTo(FINLAND.toString());
        assertThat(find("Нигдеград").read("$[0].id", String.class)).isEqualTo(somewhere.toString());
        assertThat(find("Нигдеград").read("$[0].countryId", String.class)).isNull();
    }

    @Test
    void mergesACityIntoAnotherShowingIt() {
        String merge = "{\"id\": \"%s\", \"intoId\": \"%s\"}".formatted(NEW_HELSINKI, HELSINKI);

        DocumentContext state = ToolJson.of(calls.state("merge_city", merge, ADMIN));
        UUID into = calls.write("merge_city", merge, ADMIN).uuid();

        assertThat(state.read("$.names.ru", String.class)).isEqualTo("Новые Хельсинки");
        assertThat(state.read("$.countryId", String.class)).isEqualTo(FINLAND.toString());
        assertThat(into).isEqualTo(HELSINKI);
        assertThat(ToolJson.list(find("хельсинки"), "$[*].id")).containsExactly(HELSINKI.toString());
    }

    @Test
    void mergesACountryIntoAnother() {
        String merge = "{\"id\": \"%s\", \"intoId\": \"%s\"}".formatted(RUSSIA, FINLAND);

        DocumentContext state = ToolJson.of(calls.state("merge_country", merge, ADMIN));
        calls.write("merge_country", merge, ADMIN);

        assertThat(state.read("$.names.ru", String.class)).isEqualTo("Россия");
        assertThat(ToolJson.list(find("Russia"), "$")).isEmpty();
        assertThat(find("Saint Petersburg").read("$[0].countryId", String.class))
                .isEqualTo(FINLAND.toString());
    }

    @Test
    void putsACityInACountry() {
        String put = "{\"id\": \"%s\", \"countryId\": \"%s\"}".formatted(TALLINN, FINLAND);

        DocumentContext state = ToolJson.of(calls.state("put_city_in_country", put, ADMIN));
        calls.write("put_city_in_country", put, ADMIN);

        assertThat(state.read("$.countryId", String.class)).isNull();
        assertThat(find("Tallinn").read("$[0].countryId", String.class)).isEqualTo(FINLAND.toString());
    }

    @Test
    void makesACountryOfACityOfNoKnownCountry() {
        UUID country = calls.write("turn_city_into_country", "{\"id\": \"%s\"}".formatted(TALLINN), ADMIN)
                .uuid();

        DocumentContext found = find("Tallinn");
        assertThat(ToolJson.list(found, "$[*].id")).containsExactly(country.toString());
        assertThat(found.read("$[0].kind", String.class)).isEqualTo("country");
    }

    @Test
    void leavesKeepingCountriesAndCitiesToAdministrators() {
        assertThatThrownBy(() -> calls.write("create_country", "{\"names\": {\"ru\": \"Эстония\"}}", MEMBER))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calls.write("merge_city", """
                        {"id": "%s", "intoId": "%s"}
                        """.formatted(NEW_HELSINKI, HELSINKI), MEMBER))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> calls.state("merge_city", """
                        {"id": "%s", "intoId": "%s"}
                        """.formatted(FINLAND, HELSINKI), ADMIN))
                .isInstanceOf(BusinessException.class);
    }

    private DocumentContext find(String name) {
        return ToolJson.of(calls.read("find_places", "{\"name\": \"%s\"}".formatted(name), ADMIN));
    }

    private static UUID id(int n) {
        return UUID.fromString("00000000-0000-0000-00c0-%012d".formatted(n));
    }
}
