package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.entry.GetLabelTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.label.app.MakeAuthorOfLabelCommand;
import com.archive.backend.label.app.MakeAuthorOfLabelHandler;
import com.archive.backend.label.domain.LabelId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Makes a new person of a label of nobody. */
@Component
@RequiredArgsConstructor
public class MakeAuthorOfLabelTool implements WriteTool<IdArguments> {

    private final GetLabelTool entry;
    private final MakeAuthorOfLabelHandler handler;

    @Override
    public String name() {
        return "make_author_of_label";
    }

    @Override
    public String description() {
        return "Makes a new person of a label of nobody, the label their display name; the result is the id of the person.";
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
        return ChangeKind.CONVERT;
    }

    @Override
    public String entityType() {
        return "label";
    }

    @Override
    public Object state(ToolContext context, IdArguments a) {
        return entry.read(context, a);
    }

    @Override
    public Written write(ToolContext context, IdArguments a) {
        return new Written(handler.handle(new MakeAuthorOfLabelCommand(context.actor(), new LabelId(a.id())))
                .value());
    }
}
