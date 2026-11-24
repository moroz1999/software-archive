package com.archive.backend.assistant.web.entry;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.label.app.GetLabelHandler;
import com.archive.backend.label.app.GetLabelQuery;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.software.app.ListCreditedReleasesHandler;
import com.archive.backend.software.app.ListCreditedSoftwareHandler;
import com.archive.backend.software.app.ListLabelReleasesQuery;
import com.archive.backend.software.app.ListLabelSoftwareQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** A label as the label page shows it. */
@Component
@RequiredArgsConstructor
public class GetLabelTool implements ReadTool<IdArguments> {

    private final GetLabelHandler label;
    private final ListCreditedSoftwareHandler software;
    private final ListCreditedReleasesHandler releases;

    @Override
    public String name() {
        return "get_label";
    }

    @Override
    public String description() {
        return "A label as its page shows it: its name, its owner (a person or a group, or none: a label of nobody), the"
                + " groups it was a member of, its locations and where an import took it from; and the software and"
                + " releases credited under it.";
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
        LabelId id = new LabelId(arguments.id());
        return new LabelEntry(
                label.handle(new GetLabelQuery(context.actor(), id, context.language())),
                software.handle(new ListLabelSoftwareQuery(id)),
                releases.handle(new ListLabelReleasesQuery(id)));
    }
}
