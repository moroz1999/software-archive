package com.archive.backend.assistant.web.entry;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.software.app.GetSoftwareHandler;
import com.archive.backend.software.app.GetSoftwareQuery;
import com.archive.backend.software.app.ListScreenshotsHandler;
import com.archive.backend.software.app.ListScreenshotsQuery;
import com.archive.backend.software.app.ListSoftwareReleasesHandler;
import com.archive.backend.software.app.ListSoftwareReleasesQuery;
import com.archive.backend.software.domain.Gallery;
import com.archive.backend.software.domain.SoftwareId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Software as its page shows it. */
@Component
@RequiredArgsConstructor
public class GetSoftwareTool implements ReadTool<IdArguments> {

    private final GetSoftwareHandler software;
    private final ListScreenshotsHandler screenshots;
    private final ListSoftwareReleasesHandler releases;

    @Override
    public String name() {
        return "get_software";
    }

    @Override
    public String description() {
        return "Software as its page shows it: its title, description, release date, preservation status, platform, base"
                + " hardware, categories, credits (person, group or label, with roles) and where it was imported from;"
                + " its own screenshots, maps, covers and adverts; and its releases with their hardware, screenshots, archive and credits.";
    }

    @Override
    public String schema() {
        return IdArguments.SCHEMA;
    }

    @Override
    public Class<IdArguments> argumentsType() {
        return IdArguments.class;
    }

    @Override
    public Object read(ToolContext context, IdArguments arguments) {
        SoftwareId id = new SoftwareId(arguments.id());
        return new SoftwareEntry(
                software.handle(new GetSoftwareQuery(context.actor(), id, context.language())),
                screenshots.handle(new ListScreenshotsQuery(id, null)),
                screenshots.handle(new ListScreenshotsQuery(id, null, Gallery.MAPS)),
                screenshots.handle(new ListScreenshotsQuery(id, null, Gallery.COVERS)),
                screenshots.handle(new ListScreenshotsQuery(id, null, Gallery.ADVERTS)),
                releases.handle(new ListSoftwareReleasesQuery(context.actor(), id, context.language())));
    }
}
