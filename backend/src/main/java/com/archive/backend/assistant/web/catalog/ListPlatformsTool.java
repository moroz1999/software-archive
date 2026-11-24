package com.archive.backend.assistant.web.catalog;

import com.archive.backend.assistant.web.tool.NoArguments;
import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.hardware.app.ListPlatformsHandler;
import com.archive.backend.hardware.app.ListPlatformsQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The platforms. */
@Component
@RequiredArgsConstructor
public class ListPlatformsTool implements ReadTool<NoArguments> {

    private final ListPlatformsHandler handler;

    @Override
    public String name() {
        return "list_platforms";
    }

    @Override
    public String description() {
        return "The platforms of the archive with their codes and names.";
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
        return handler.handle(new ListPlatformsQuery(context.language()));
    }
}
