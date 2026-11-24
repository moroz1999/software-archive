package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.entry.GetLabelTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.label.app.MakeGroupOfLabelCommand;
import com.archive.backend.label.app.MakeGroupOfLabelHandler;
import com.archive.backend.label.domain.LabelId;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Makes a new group of a label of nobody. */
@Component
@RequiredArgsConstructor
public class MakeGroupOfLabelTool implements WriteTool<MakeGroupOfLabelArguments> {

    private final GetLabelTool entry;
    private final MakeGroupOfLabelHandler handler;

    @Override
    public String name() {
        return "make_group_of_label";
    }

    @Override
    public String description() {
        return "Makes a new group of a label of nobody, named by it, of the types given; the result is the id of the group.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid", "description": "The label of nobody."},
                    "types": {"type": ["array", "null"], "items": {"type": "string"},
                     "description": "Its group types, of list_options."}
                  },
                  "required": ["id"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<MakeGroupOfLabelArguments> argumentsType() {
        return MakeGroupOfLabelArguments.class;
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
    public Object state(ToolContext context, MakeGroupOfLabelArguments a) {
        return entry.read(context, new IdArguments(a.id()));
    }

    @Override
    public Written write(ToolContext context, MakeGroupOfLabelArguments a) {
        Set<GroupTypeCode> types = a.types() == null
                ? Set.of()
                : a.types().stream().map(GroupTypeCode::new).collect(Collectors.toCollection(LinkedHashSet::new));
        return Written.of(handler.handle(new MakeGroupOfLabelCommand(context.actor(), new LabelId(a.id()), types))
                .value());
    }
}
