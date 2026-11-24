package com.archive.backend.assistant.web.catalog;

import com.archive.backend.assistant.web.tool.NoArguments;
import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.group.app.ListGroupTypesHandler;
import com.archive.backend.group.app.ListGroupTypesQuery;
import com.archive.backend.group.domain.GroupMemberDraft;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The fixed lists of the archive. */
@Component
@RequiredArgsConstructor
public class ListOptionsTool implements ReadTool<NoArguments> {

    private final ListGroupTypesHandler groupTypes;

    @Override
    public String name() {
        return "list_options";
    }

    @Override
    public String description() {
        return "The fixed lists of the archive: release types, credit roles, group member roles, preservation statuses"
                + " of software and group types.";
    }

    @Override
    public String schema() {
        return NoArguments.SCHEMA;
    }

    @Override
    public Class<NoArguments> argumentsType() {
        return NoArguments.class;
    }

    @Override
    public Object read(ToolContext context, NoArguments arguments) {
        return new Options(
                Arrays.stream(ReleaseType.values()).map(ReleaseType::code).toList(),
                SoftwareCreditDraft.ALLOWED_ROLES.stream().sorted().toList(),
                GroupMemberDraft.ALLOWED_ROLES.stream().sorted().toList(),
                Arrays.stream(PreservationStatus.values())
                        .map(PreservationStatus::code)
                        .toList(),
                groupTypes.handle(new ListGroupTypesQuery()));
    }

    /** The fixed lists, by their codes. */
    public record Options(
            List<String> releaseTypes,
            List<String> creditRoles,
            List<String> memberRoles,
            List<String> preservationStatuses,
            List<String> groupTypes) {}
}
