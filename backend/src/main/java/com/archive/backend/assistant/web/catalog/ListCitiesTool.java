package com.archive.backend.assistant.web.catalog;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.geo.app.ListCitiesHandler;
import com.archive.backend.geo.app.ListCitiesQuery;
import com.archive.backend.geo.domain.CountryId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The cities of a country. */
@Component
@RequiredArgsConstructor
public class ListCitiesTool implements ReadTool<ListCitiesArguments> {

    private final ListCitiesHandler handler;

    @Override
    public String name() {
        return "list_cities";
    }

    @Override
    public String description() {
        return "The cities of a country with ids and names.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {"countryId": {"type": "string", "format": "uuid"}},
                  "required": ["countryId"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<ListCitiesArguments> argumentsType() {
        return ListCitiesArguments.class;
    }

    @Override
    public Object read(ToolContext context, ListCitiesArguments arguments) {
        return handler.handle(new ListCitiesQuery(new CountryId(arguments.countryId()), context.language()));
    }
}
