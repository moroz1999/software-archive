package com.archive.backend.assistant.web.search;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolPage;
import com.archive.backend.assistant.web.tool.ToolPages;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.group.app.ListGroupsHandler;
import com.archive.backend.group.app.ListGroupsQuery;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelLetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The groups catalog, as its page shows it. */
@Component
@RequiredArgsConstructor
public class ListGroupsTool implements ReadTool<CatalogArguments> {

    private final ListGroupsHandler handler;

    @Override
    public String name() {
        return "list_groups";
    }

    @Override
    public String description() {
        return "The group catalog, as its page shows it: groups and their labels mixed, sorted by name; a label has its group as note. Narrowed by the first letter, the country and city, and the platform.";
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
        return ToolPage.of(handler.handle(new ListGroupsQuery(
                a.letter() == null ? null : new LabelLetter(a.letter()),
                a.countryId() == null ? null : new CountryId(a.countryId()),
                a.cityId() == null ? null : new CityId(a.cityId()),
                a.platform() == null ? null : new PlatformCode(a.platform()),
                ToolPages.of(a.page(), a.size()))));
    }
}
