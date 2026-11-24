package com.archive.backend.assistant.web.author;

import com.archive.backend.assistant.web.entry.GetAuthorTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.author.app.DeleteAuthorCommand;
import com.archive.backend.author.app.DeleteAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Deletes a person, leaving their labels to nobody; the plan shows them as get_author does. */
@Component
@RequiredArgsConstructor
public class DeleteAuthorTool implements WriteTool<IdArguments> {

    private final GetAuthorTool entry;
    private final DeleteAuthorHandler handler;

    @Override
    public String name() {
        return "delete_author";
    }

    @Override
    public String description() {
        return "Deletes a person; their labels stay, as labels of nobody, with their credits.";
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
    public ChangeKind kind() {
        return ChangeKind.DELETE;
    }

    @Override
    public String entityType() {
        return "author";
    }

    @Override
    public Object state(ToolContext context, IdArguments a) {
        return entry.read(context, a);
    }

    @Override
    public Written write(ToolContext context, IdArguments a) {
        handler.handle(new DeleteAuthorCommand(context.actor(), new AuthorId(a.id())));
        return Written.of(a.id());
    }
}
