package com.archive.backend.assistant.web.search;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.software.app.LookupSoftwareReferencesHandler;
import com.archive.backend.software.app.LookupSoftwareReferencesQuery;
import com.archive.backend.software.domain.SoftwareReferencePhrase;
import com.archive.backend.software.domain.SoftwareReferenceType;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** People, groups and labels by a part of the name, as the credit picker of the software form finds them. */
@Component
@RequiredArgsConstructor
public class FindNamesTool implements ReadTool<FindNamesArguments> {

    private final LookupSoftwareReferencesHandler handler;

    @Override
    public String name() {
        return "find_names";
    }

    @Override
    public String description() {
        return "People, groups and labels whose name contains the phrase, labels of nobody too, at most 30: each with"
                + " its id, type (author, author-label, group, group-label), title and the id of its owner, if any."
                + " These ids are what credits and memberships take.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "phrase": {"type": "string", "description": "A part of the name."},
                    "types": {
                      "type": ["array", "null"],
                      "items": {"enum": ["author", "author-label", "group", "group-label"]},
                      "description": "The kinds to find; all when not given."
                    }
                  },
                  "required": ["phrase"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<FindNamesArguments> argumentsType() {
        return FindNamesArguments.class;
    }

    @Override
    public Object read(ToolContext context, FindNamesArguments arguments) {
        return handler.handle(new LookupSoftwareReferencesQuery(
                new SoftwareReferencePhrase(arguments.phrase()), types(arguments.types())));
    }

    private static Set<SoftwareReferenceType> types(@Nullable List<String> codes) {
        return codes == null || codes.isEmpty()
                ? EnumSet.allOf(SoftwareReferenceType.class)
                : codes.stream().map(SoftwareReferenceType::fromCode).collect(Collectors.toSet());
    }
}
