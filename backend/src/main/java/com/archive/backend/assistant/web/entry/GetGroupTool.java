package com.archive.backend.assistant.web.entry;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.group.app.GetGroupHandler;
import com.archive.backend.group.app.GetGroupQuery;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.software.app.ListCreditedReleasesHandler;
import com.archive.backend.software.app.ListCreditedSoftwareHandler;
import com.archive.backend.software.app.ListGroupReleasesQuery;
import com.archive.backend.software.app.ListGroupSoftwareQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** A group as the group page shows it. */
@Component
@RequiredArgsConstructor
public class GetGroupTool implements ReadTool<IdArguments> {

    private final GetGroupHandler group;
    private final ListCreditedSoftwareHandler software;
    private final ListCreditedReleasesHandler releases;

    @Override
    public String name() {
        return "get_group";
    }

    @Override
    public String description() {
        return "A group as its page shows it: its name, types, notes, labels, locations, members (people or labels,"
                + " with roles), member groups and the groups it is a member of; and the software and releases it is"
                + " credited on, by itself or under any label, with its roles.";
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
        GroupId id = new GroupId(arguments.id());
        return new GroupEntry(
                group.handle(new GetGroupQuery(context.actor(), id, context.language())),
                software.handle(new ListGroupSoftwareQuery(id)),
                releases.handle(new ListGroupReleasesQuery(id)));
    }
}
