package com.archive.backend.assistant.web.entry;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.software.app.GetReleaseFilesHandler;
import com.archive.backend.software.app.GetReleaseFilesQuery;
import com.archive.backend.software.app.GetSoftwareReleaseHandler;
import com.archive.backend.software.app.GetSoftwareReleaseQuery;
import com.archive.backend.software.app.ListScreenshotsHandler;
import com.archive.backend.software.app.ListScreenshotsQuery;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.ReleaseDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** A release as its page shows it. */
@Component
@RequiredArgsConstructor
public class GetReleaseTool implements ReadTool<GetReleaseArguments> {

    private final GetSoftwareReleaseHandler release;
    private final ListScreenshotsHandler screenshots;
    private final GetReleaseFilesHandler files;

    @Override
    public String name() {
        return "get_release";
    }

    @Override
    public String description() {
        return "A release as its page shows it: its title, version, type, release date, description, hardware, archive and"
                + " credits; its screenshots; and the file tree of its archive, if it has one, every file with its MD5 digest.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "softwareId": {"type": "string", "format": "uuid"},
                    "releaseId": {"type": "string", "format": "uuid"}
                  },
                  "required": ["softwareId", "releaseId"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<GetReleaseArguments> argumentsType() {
        return GetReleaseArguments.class;
    }

    @Override
    public Object read(ToolContext context, GetReleaseArguments arguments) {
        SoftwareId software = new SoftwareId(arguments.softwareId());
        ReleaseId id = new ReleaseId(arguments.releaseId());
        ReleaseDetails details = release.handle(new GetSoftwareReleaseQuery(software, id, context.language()));
        return new ReleaseEntry(
                details,
                screenshots.handle(new ListScreenshotsQuery(software, id)),
                details.archive() == null ? null : files.handle(new GetReleaseFilesQuery(software, id)));
    }
}
