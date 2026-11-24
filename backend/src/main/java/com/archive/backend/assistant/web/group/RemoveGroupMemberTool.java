package com.archive.backend.assistant.web.group;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.group.app.UpdateGroupHandler;
import com.archive.backend.group.web.mapper.GroupWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Takes a member out of a group; the other members stay. */
@Component
@RequiredArgsConstructor
public class RemoveGroupMemberTool implements WriteTool<RemoveGroupMemberArguments> {

    private final GroupForms forms;
    private final UpdateGroupHandler handler;
    private final GroupWebMapper mapper;

    @Override
    public String name() {
        return "remove_group_member";
    }

    @Override
    public String description() {
        return "Takes a member out of a group; the other members stay.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "groupId": {"type": "string", "format": "uuid"},
                    "id": {"type": "string", "format": "uuid", "description": "The member."}
                  },
                  "required": ["groupId", "id"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<RemoveGroupMemberArguments> argumentsType() {
        return RemoveGroupMemberArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.UNLINK;
    }

    @Override
    public String entityType() {
        return "group";
    }

    @Override
    public GroupState state(ToolContext context, RemoveGroupMemberArguments a) {
        GroupState current = forms.group(context, a.groupId());
        GroupForms.requireMember(current.members(), a.id());
        return current;
    }

    @Override
    public Written write(ToolContext context, RemoveGroupMemberArguments a) {
        GroupState current = state(context, a);
        handler.handle(mapper.toUpdate(
                context.actor(),
                a.groupId(),
                GroupForms.request(
                        current,
                        GroupForms.currentMembers(current.members()).stream()
                                .filter(member -> !a.id().equals(member.id()))
                                .toList())));
        return Written.of(a.groupId());
    }
}
