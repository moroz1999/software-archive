package com.archive.backend.assistant.web.catalog;

import com.archive.backend.assistant.web.tool.NoArguments;
import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.category.app.ListCategoriesHandler;
import com.archive.backend.category.app.ListCategoriesQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The category tree. */
@Component
@RequiredArgsConstructor
public class ListCategoriesTool implements ReadTool<NoArguments> {

    private final ListCategoriesHandler handler;

    @Override
    public String name() {
        return "list_categories";
    }

    @Override
    public String description() {
        return "The tree of categories software is sorted by, with ids and titles; software goes into leaf categories.";
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
        return handler.handle(new ListCategoriesQuery(context.language()));
    }
}
