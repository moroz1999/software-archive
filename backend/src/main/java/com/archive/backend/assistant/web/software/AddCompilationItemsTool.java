package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.AddCompilationItemsCommand;
import com.archive.backend.software.app.AddCompilationItemsHandler;
import com.archive.backend.software.app.GetSoftwareHandler;
import com.archive.backend.software.app.GetSoftwareQuery;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareLink;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AddCompilationItemsTool implements WriteTool<AddCompilationItemsArguments> {

    private final GetSoftwareHandler getSoftware;
    private final AddCompilationItemsHandler addItems;

    @Override
    public String name() {
        return "add_compilation_items";
    }

    @Override
    public String description() {
        return "Adds existing software entries to a compilation's programs. The same program is linked once.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid", "description": "The compilation's software id."},
                    "compilationItemIds": {
                      "type": "array", "minItems": 1, "items": {"type": "string", "format": "uuid"},
                      "description": "The software ids of the programs to add."
                    }
                  },
                  "required": ["id", "compilationItemIds"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<AddCompilationItemsArguments> argumentsType() {
        return AddCompilationItemsArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.LINK;
    }

    @Override
    public String entityType() {
        return "software";
    }

    @Override
    public SoftwareLink state(ToolContext context, AddCompilationItemsArguments arguments) {
        SoftwareDetails compilation = getSoftware.handle(
                new GetSoftwareQuery(context.actor(), new SoftwareId(arguments.id()), context.language()));
        return new SoftwareLink(compilation.id(), compilation.title());
    }

    @Override
    public Written write(ToolContext context, AddCompilationItemsArguments arguments) {
        Set<SoftwareId> programs =
                arguments.compilationItemIds().stream().map(SoftwareId::new).collect(Collectors.toUnmodifiableSet());
        SoftwareId compilation = addItems.handle(
                new AddCompilationItemsCommand(context.actor(), new SoftwareId(arguments.id()), programs));
        return Written.of(compilation.value());
    }
}
