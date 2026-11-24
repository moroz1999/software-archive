package com.archive.backend.assistant.web.entry;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.author.app.GetAuthorHandler;
import com.archive.backend.author.app.GetAuthorQuery;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.software.app.ListAuthorReleasesQuery;
import com.archive.backend.software.app.ListAuthorSoftwareQuery;
import com.archive.backend.software.app.ListCreditedReleasesHandler;
import com.archive.backend.software.app.ListCreditedSoftwareHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** A author as the author page shows it. */
@Component
@RequiredArgsConstructor
public class GetAuthorTool implements ReadTool<IdArguments> {

    private final GetAuthorHandler author;
    private final ListCreditedSoftwareHandler software;
    private final ListCreditedReleasesHandler releases;

    @Override
    public String name() {
        return "get_author";
    }

    @Override
    public String description() {
        return "A person as their page shows them: the main name, the real name, notes, all their labels (the display"
                + " name marked), locations and group memberships; and the software and releases they are credited on,"
                + " by themselves or under any label, with their roles.";
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
        AuthorId id = new AuthorId(arguments.id());
        return new AuthorEntry(
                author.handle(new GetAuthorQuery(context.actor(), id, context.language())),
                software.handle(new ListAuthorSoftwareQuery(id)),
                releases.handle(new ListAuthorReleasesQuery(id)));
    }
}
