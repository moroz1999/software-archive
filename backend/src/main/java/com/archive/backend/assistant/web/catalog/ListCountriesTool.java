package com.archive.backend.assistant.web.catalog;

import com.archive.backend.assistant.web.tool.NoArguments;
import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.geo.app.ListCountriesHandler;
import com.archive.backend.geo.app.ListCountriesQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The countries. */
@Component
@RequiredArgsConstructor
public class ListCountriesTool implements ReadTool<NoArguments> {

    private final ListCountriesHandler handler;

    @Override
    public String name() {
        return "list_countries";
    }

    @Override
    public String description() {
        return "The countries of the archive with ids and names.";
    }

    @Override
    public String schema() {
        return NoArguments.SCHEMA;
    }

    @Override
    public Class<NoArguments> argumentsType() {
        return NoArguments.class;
    }

    @Override
    public Object read(ToolContext context, NoArguments arguments) {
        return handler.handle(new ListCountriesQuery(context.language()));
    }
}
