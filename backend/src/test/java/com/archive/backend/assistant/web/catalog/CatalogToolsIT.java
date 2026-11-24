package com.archive.backend.assistant.web.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolJson;
import com.archive.backend.geo.GeoTestRows;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.TestActors;
import com.jayway.jsonpath.DocumentContext;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

/** The reference books of the archive as the assistant reads them, named in the editor's language. */
@IntegrationTest
@Transactional
@Rollback
class CatalogToolsIT {

    private static final UUID FINLAND = UUID.fromString("00000000-0000-0000-0000-000000000021");
    private static final UUID GAMES = UUID.fromString("00000000-0000-0000-0000-000000000041");
    private static final UUID HELSINKI = UUID.fromString("00000000-0000-0000-0000-000000000031");

    @Autowired
    private ToolCalls calls;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void listsThePlatformsAndTheHardwareOfOne() {
        assertThat(ToolJson.list(read("list_platforms", "{}", Language.EN), "$[*].code"))
                .contains("vector", "bk");
        assertThat(ToolJson.list(read("list_hardware", "{\"platform\": \"vector\"}", Language.EN), "$[*].code"))
                .contains("vector06c")
                .doesNotContain("bk0010-01");
    }

    @Test
    void listsTheCategoryTree() {
        jdbc.update("INSERT INTO category (id, parent_id, position) VALUES (?, NULL, 0)", GAMES);
        jdbc.update("INSERT INTO category_title (category_id, language, title) VALUES (?, 'ru', 'Игры')", GAMES);
        jdbc.update("INSERT INTO category_title (category_id, language, title) VALUES (?, 'en', 'Games')", GAMES);

        assertThat(ToolJson.list(read("list_categories", "{}", Language.EN), "$[*].title"))
                .contains("Games");
    }

    @Test
    void listsTheCountriesAndTheCitiesOfOneInTheLanguageAsked() {
        GeoTestRows.country(jdbc, FINLAND, "Финляндия", "Finland");
        GeoTestRows.city(jdbc, HELSINKI, FINLAND, "Хельсинки", "Helsinki");

        assertThat(ToolJson.list(read("list_countries", "{}", Language.RU), "$[*].name"))
                .contains("Финляндия");
        assertThat(ToolJson.list(
                        read("list_cities", "{\"countryId\": \"%s\"}".formatted(FINLAND), Language.EN), "$[*].name"))
                .containsExactly("Helsinki");
    }

    private DocumentContext read(String tool, String arguments, Language language) {
        return ToolJson.of(calls.read(tool, arguments, new ToolContext(TestActors.MEMBER, language)));
    }
}
