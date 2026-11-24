package com.archive.backend.assistant.web.search;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolPage;
import com.archive.backend.assistant.web.tool.ToolPages;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.app.ListLatestSoftwareHandler;
import com.archive.backend.software.app.ListLatestSoftwareQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The software most recently added, as the home page shows it. */
@Component
@RequiredArgsConstructor
public class ListLatestSoftwareTool implements ReadTool<ListLatestSoftwareArguments> {

    private final ListLatestSoftwareHandler handler;

    @Override
    public String name() {
        return "list_latest_software";
    }

    @Override
    public String description() {
        return "The software most recently added to the archive, newest first, as the home page shows it.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "platform": {"type": ["string", "null"], "description": "The platform of the platform switch."},
                    %s
                  },
                  "additionalProperties": false
                }
                """.formatted(ToolPages.SCHEMA);
    }

    @Override
    public Class<ListLatestSoftwareArguments> argumentsType() {
        return ListLatestSoftwareArguments.class;
    }

    @Override
    public Object read(ToolContext context, ListLatestSoftwareArguments a) {
        return ToolPage.of(handler.handle(new ListLatestSoftwareQuery(
                a.platform() == null ? null : new PlatformCode(a.platform()), ToolPages.of(a.page(), a.size()))));
    }
}
