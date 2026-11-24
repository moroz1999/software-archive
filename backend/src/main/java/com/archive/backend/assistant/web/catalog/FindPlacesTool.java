package com.archive.backend.assistant.web.catalog;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.geo.app.FindPlacesHandler;
import com.archive.backend.geo.app.FindPlacesQuery;
import com.archive.backend.geo.domain.PlaceNamePhrase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Countries and cities by a name of theirs in any language. */
@Component
@RequiredArgsConstructor
public class FindPlacesTool implements ReadTool<FindPlacesArguments> {

    private final FindPlacesHandler handler;

    @Override
    public String name() {
        return "find_places";
    }

    @Override
    public String description() {
        return "Countries and cities with a name in any language containing the text given (letter case, ё and е,"
                + " spaces and hyphens aside), those of that very name first: each its kind (country or city), name,"
                + " names by language and, of a city, its country, none while it is not known.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {"name": {"type": "string", "minLength": 1}},
                  "required": ["name"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<FindPlacesArguments> argumentsType() {
        return FindPlacesArguments.class;
    }

    @Override
    public Object read(ToolContext context, FindPlacesArguments arguments) {
        return handler.handle(new FindPlacesQuery(new PlaceNamePhrase(arguments.name()), context.language()));
    }
}
