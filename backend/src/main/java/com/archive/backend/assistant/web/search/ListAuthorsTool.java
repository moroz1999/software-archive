package com.archive.backend.assistant.web.search;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolPage;
import com.archive.backend.assistant.web.tool.ToolPages;
import com.archive.backend.author.app.ListAuthorsHandler;
import com.archive.backend.author.app.ListAuthorsQuery;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelLetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The authors catalog, as its page shows it. */
@Component
@RequiredArgsConstructor
public class ListAuthorsTool implements ReadTool<CatalogArguments> {

    private final ListAuthorsHandler handler;

    @Override
    public String name() {
        return "list_authors";
    }

    @Override
    public String description() {
        return "The author catalog, as its page shows it: people and their labels mixed, sorted by name, each label an entry of its own, labels of nobody too. A person is an entry by the real name with the display name as note; a label has its owner as note. Narrowed by the first letter, the country and city of the owner, and the platform.";
    }

    @Override
    public String schema() {
        return CatalogArguments.SCHEMA;
    }

    @Override
    public Class<CatalogArguments> argumentsType() {
        return CatalogArguments.class;
    }

    @Override
    public Object read(ToolContext context, CatalogArguments a) {
        return ToolPage.of(handler.handle(new ListAuthorsQuery(
                a.letter() == null ? null : new LabelLetter(a.letter()),
                a.countryId() == null ? null : new CountryId(a.countryId()),
                a.cityId() == null ? null : new CityId(a.cityId()),
                a.platform() == null ? null : new PlatformCode(a.platform()),
                ToolPages.of(a.page(), a.size()))));
    }
}
