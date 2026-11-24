package com.archive.backend.assistant.web.search;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolPage;
import com.archive.backend.assistant.web.tool.ToolPages;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.ListSoftwareCatalogHandler;
import com.archive.backend.search.app.ListSoftwareCatalogQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** The software catalog with its filters, as the software list page shows it. */
@Component
@RequiredArgsConstructor
public class SearchSoftwareTool implements ReadTool<SearchSoftwareArguments> {

    private final ListSoftwareCatalogHandler handler;

    @Override
    public String name() {
        return "search_software";
    }

    @Override
    public String description() {
        return "The software catalog, as the software list page shows it with the same filters: software in a"
                + " category and its whole subtree, narrowed by a word of the title, the year of the release date,"
                + " the country and city of the people and groups credited, platforms and hardware (any of the items"
                + " of one group, every group given). Each with its id, title, release date and who made it.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "keyword": {"type": ["string", "null"], "description": "A word of the title."},
                    "platform": {"type": ["string", "null"], "description": "The platform of the platform switch."},
                    "year": {"type": ["integer", "null"], "description": "The year of the release date."},
                    "categoryId": {"type": ["string", "null"], "format": "uuid"},
                    "countryId": {"type": ["string", "null"], "format": "uuid"},
                    "cityId": {"type": ["string", "null"], "format": "uuid"},
                    "platforms": {
                      "type": ["array", "null"],
                      "items": {"type": "string"},
                      "description": "Any of these platforms (codes), while the switch shows every platform."
                    },
                    "hardware": {"type": ["array", "null"], "items": {"type": "string"}, "description": "Codes."},
                    %s
                  },
                  "additionalProperties": false
                }
                """.formatted(ToolPages.SCHEMA);
    }

    @Override
    public Class<SearchSoftwareArguments> argumentsType() {
        return SearchSoftwareArguments.class;
    }

    @Override
    public Object read(ToolContext context, SearchSoftwareArguments a) {
        return ToolPage.of(handler.handle(new ListSoftwareCatalogQuery(
                        CatalogKeyword.of(a.keyword()),
                        a.platform() == null ? null : new PlatformCode(a.platform()),
                        a.year() == null ? null : new ReleaseYear(a.year()),
                        a.categoryId() == null ? null : new CategoryId(a.categoryId()),
                        a.countryId() == null ? null : new CountryId(a.countryId()),
                        a.cityId() == null ? null : new CityId(a.cityId()),
                        codes(a.platforms()).stream().map(PlatformCode::new).toList(),
                        codes(a.hardware()).stream().map(HardwareCode::new).toList(),
                        context.language(),
                        ToolPages.of(a.page(), a.size())))
                .catalog()
                .items());
    }

    private static List<String> codes(@Nullable List<String> codes) {
        return codes == null ? List.of() : codes;
    }
}
