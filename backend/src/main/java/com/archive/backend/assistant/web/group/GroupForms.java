package com.archive.backend.assistant.web.group;

import com.archive.backend.assistant.web.tool.LocationArguments;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolSchemas;
import com.archive.backend.group.app.GetGroupHandler;
import com.archive.backend.group.app.GetGroupQuery;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupMemberDraft;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.web.dto.GroupRequestDto;
import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** A group as its form has it: read as it is, and sent back whole with the fields a tool changes. */
@Component
@RequiredArgsConstructor
class GroupForms {

    static final String LABELS = """
            {
              "type": ["array", "null"],
              "description": "Every label of the group besides its name; given, they replace them.",
              "items": {
                "type": "object",
                "properties": {
                  "id": {"type": ["string", "null"], "format": "uuid", "description": "A label it has; none: a new\
             one."},
                  "name": {"type": ["string", "null"], "description": "Its name; of a label it has, a new name."}
                },
                "additionalProperties": false
              }
            }""";

    /** The properties of a member, to put into the schema of a member or of a tool. */
    static final String MEMBER = """
            "id": {"type": "string", "format": "uuid", "description": "The person or the label of a person."},
            "type": {"enum": ["author", "author-label"], "description": "author: the person by themselves;\
             author-label: under the exact label they were a member under."},
            "roles": {"type": ["array", "null"], "items": {"enum": %s}, "description": "None: unknown."}
            """.formatted(ToolSchemas.strings(GroupMemberDraft.ALLOWED_ROLES));

    static final String MEMBERS = """
            {
              "type": ["array", "null"],
              "description": "Every member; given, they replace them.",
              "items": {
                "type": "object",
                "properties": {%s},
                "required": ["id", "type"],
                "additionalProperties": false
              }
            }""".formatted(MEMBER);

    private final GetGroupHandler group;

    GroupState group(ToolContext context, UUID id) {
        GroupDetails details = group.handle(new GetGroupQuery(context.actor(), new GroupId(id), context.language()));
        return new GroupState(
                details.name(),
                details.notes(),
                details.types(),
                details.labels().stream()
                        .map(label -> new GroupLabelArguments(label.id(), label.name()))
                        .toList(),
                details.locations().stream()
                        .map(location ->
                                new LocationArguments(location.countryId(), location.cityId(), location.primary()))
                        .toList(),
                details.members().stream()
                        .map(member -> new MemberState(
                                Objects.requireNonNull(member.id(), "A member read has an id"),
                                member.type(),
                                member.title(),
                                member.roles()))
                        .toList());
    }

    static GroupRequestDto request(
            @Nullable String name,
            @Nullable String notes,
            @Nullable List<String> types,
            @Nullable List<GroupLabelArguments> labels,
            @Nullable List<LocationArguments> locations,
            @Nullable List<GroupRequestDto.MemberDto> members) {
        return new GroupRequestDto(
                name,
                notes,
                types,
                labels == null
                        ? null
                        : labels.stream()
                                .map(label -> new GroupRequestDto.LabelDto(label.id(), label.name()))
                                .toList(),
                locations == null
                        ? null
                        : locations.stream()
                                .map(location -> new GroupRequestDto.LocationDto(
                                        location.countryId(), location.cityId(), location.primary()))
                                .toList(),
                members);
    }

    /** The group as it is, with these members. */
    static GroupRequestDto request(GroupState state, List<GroupRequestDto.MemberDto> members) {
        return request(state.name(), state.notes(), state.types(), state.labels(), state.locations(), members);
    }

    static @Nullable List<GroupRequestDto.MemberDto> members(@Nullable List<MemberArguments> members) {
        return members == null
                ? null
                : members.stream()
                        .map(member -> new GroupRequestDto.MemberDto(member.id(), member.type(), member.roles()))
                        .toList();
    }

    static List<GroupRequestDto.MemberDto> currentMembers(List<MemberState> members) {
        return members.stream()
                .map(member -> new GroupRequestDto.MemberDto(member.id(), member.type(), member.roles()))
                .toList();
    }

    /** The members with this one put in place of the same one, or after the others. */
    static List<GroupRequestDto.MemberDto> withMember(List<MemberState> members, GroupRequestDto.MemberDto member) {
        List<GroupRequestDto.MemberDto> result = new ArrayList<>();
        boolean replaced = false;
        for (GroupRequestDto.MemberDto current : currentMembers(members)) {
            if (Objects.equals(member.id(), current.id())) {
                result.add(member);
                replaced = true;
            } else {
                result.add(current);
            }
        }
        if (!replaced) {
            result.add(member);
        }
        return result;
    }

    static void requireMember(List<MemberState> members, UUID id) {
        if (members.stream().noneMatch(member -> member.id().equals(id))) {
            throw new RuleViolationException("assistant.not-a-member", "Nobody of the id " + id + " is a member.");
        }
    }
}
