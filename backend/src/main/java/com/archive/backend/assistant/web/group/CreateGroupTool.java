package com.archive.backend.assistant.web.group;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.WriteTools;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.group.app.CreateGroupHandler;
import com.archive.backend.group.web.dto.GroupRequestDto;
import com.archive.backend.group.web.mapper.GroupWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Adds a group with its labels, locations and members, as the group form does. */
@Component
@RequiredArgsConstructor
public class CreateGroupTool implements WriteTool<CreateGroupArguments> {

    private final CreateGroupHandler handler;
    private final GroupWebMapper mapper;

    @Override
    public String name() {
        return "create_group";
    }

    @Override
    public String description() {
        return "Adds a group: its name, notes, types, labels, locations and members. Look for it first: equal names are not"
                + " the same group.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "name": {"type": "string"},
                    "notes": {"type": ["string", "null"]},
                    "types": {"type": ["array", "null"], "items": {"type": "string"},
                     "description": "Its group types, of list_options; given, they replace them."},
                    "labels": %s,
                    "locations": %s,
                    "members": %s
                  },
                  "required": ["name"],
                  "additionalProperties": false
                }
                """.formatted(GroupForms.LABELS, WriteTools.LOCATIONS, GroupForms.MEMBERS);
    }

    @Override
    public Class<CreateGroupArguments> argumentsType() {
        return CreateGroupArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.CREATE;
    }

    @Override
    public String entityType() {
        return "group";
    }

    @Override
    public @Nullable Object state(ToolContext context, CreateGroupArguments a) {
        return null;
    }

    @Override
    public Written write(ToolContext context, CreateGroupArguments a) {
        GroupRequestDto request = GroupForms.request(
                a.name(),
                Changes.text(a.notes()),
                a.types(),
                a.labels(),
                a.locations(),
                GroupForms.members(a.members()));
        return Written.of(
                handler.handle(mapper.toCreate(context.actor(), request)).value());
    }
}
