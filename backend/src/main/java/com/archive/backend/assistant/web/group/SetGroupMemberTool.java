package com.archive.backend.assistant.web.group;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.group.app.UpdateGroupHandler;
import com.archive.backend.group.web.dto.GroupRequestDto;
import com.archive.backend.group.web.mapper.GroupWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Makes somebody a member of a group, or gives the member other roles; the other members stay. */
@Component
@RequiredArgsConstructor
public class SetGroupMemberTool implements WriteTool<GroupMemberArguments> {

    private final GroupForms forms;
    private final UpdateGroupHandler handler;
    private final GroupWebMapper mapper;

    @Override
    public String name() {
        return "set_group_member";
    }

    @Override
    public String description() {
        return "Makes a person, or a person under one of their labels, a member of a group with the roles given, or, a member"
                + " already, gives them these roles instead; the other members stay.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "groupId": {"type": "string", "format": "uuid"},
                    %s
                  },
                  "required": ["groupId", "id", "type"],
                  "additionalProperties": false
                }
                """.formatted(GroupForms.MEMBER);
    }

    @Override
    public Class<GroupMemberArguments> argumentsType() {
        return GroupMemberArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.LINK;
    }

    @Override
    public String entityType() {
        return "group";
    }

    @Override
    public GroupState state(ToolContext context, GroupMemberArguments a) {
        return forms.group(context, a.groupId());
    }

    @Override
    public Written write(ToolContext context, GroupMemberArguments a) {
        GroupState current = state(context, a);
        GroupRequestDto.MemberDto member = new GroupRequestDto.MemberDto(a.id(), a.type(), a.roles());
        handler.handle(mapper.toUpdate(
                context.actor(),
                a.groupId(),
                GroupForms.request(current, GroupForms.withMember(current.members(), member))));
        return new Written(a.groupId());
    }
}
