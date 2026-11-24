package com.archive.backend.assistant.web.group;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.WriteTools;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.group.app.UpdateGroupHandler;
import com.archive.backend.group.web.dto.GroupRequestDto;
import com.archive.backend.group.web.mapper.GroupWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Changes the fields of a group given; the rest stays. */
@Component
@RequiredArgsConstructor
public class UpdateGroupTool implements WriteTool<UpdateGroupArguments> {

    private final GroupForms forms;
    private final UpdateGroupHandler handler;
    private final GroupWebMapper mapper;

    @Override
    public String name() {
        return "update_group";
    }

    @Override
    public String description() {
        return "Changes the fields of a group given; a field left out stays, an empty text clears it, a list given replaces"
                + " the whole list. To add one member or take one out, set_group_member and remove_group_member.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid"},
                    "name": {"type": ["string", "null"]},
                    "notes": {"type": ["string", "null"]},
                    "types": {"type": ["array", "null"], "items": {"type": "string"},
                     "description": "Its group types, of list_options; given, they replace them."},
                    "labels": %s,
                    "locations": %s,
                    "members": %s
                  },
                  "required": ["id"],
                  "additionalProperties": false
                }
                """.formatted(GroupForms.LABELS, WriteTools.LOCATIONS, GroupForms.MEMBERS);
    }

    @Override
    public Class<UpdateGroupArguments> argumentsType() {
        return UpdateGroupArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.UPDATE;
    }

    @Override
    public String entityType() {
        return "group";
    }

    @Override
    public GroupState state(ToolContext context, UpdateGroupArguments a) {
        return forms.group(context, a.id());
    }

    @Override
    public Written write(ToolContext context, UpdateGroupArguments a) {
        GroupState current = state(context, a);
        GroupRequestDto request = GroupForms.request(
                Changes.given(a.name(), current.name()),
                Changes.text(a.notes(), current.notes()),
                Changes.given(a.types(), current.types()),
                Changes.given(a.labels(), current.labels()),
                Changes.given(a.locations(), current.locations()),
                Changes.given(GroupForms.members(a.members()), GroupForms.currentMembers(current.members())));
        handler.handle(mapper.toUpdate(context.actor(), a.id(), request));
        return Written.of(a.id());
    }
}
