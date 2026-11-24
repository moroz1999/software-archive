package com.archive.backend.assistant.web.place;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.NamesArguments;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.geo.app.CreateCityHandler;
import com.archive.backend.geo.web.dto.PlaceRequestDto;
import com.archive.backend.geo.web.mapper.PlaceWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Adds a city, in its country or in none while it is not known, as the management of places does. */
@Component
@RequiredArgsConstructor
public class CreateCityTool implements WriteTool<CreateCityArguments> {

    private final CreateCityHandler handler;
    private final PlaceWebMapper mapper;

    @Override
    public String name() {
        return "create_city";
    }

    @Override
    public String description() {
        return "Adds a city by its names, in its country (find it with find_places, or add it first). Look for the city with"
                + " find_places first.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "names": %s,
                    "countryId": {"type": ["string", "null"], "format": "uuid",
                     "description": "Its country; none only when it is not known."}
                  },
                  "required": ["names"],
                  "additionalProperties": false
                }
                """.formatted(NamesArguments.SCHEMA);
    }

    @Override
    public Class<CreateCityArguments> argumentsType() {
        return CreateCityArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.CREATE;
    }

    @Override
    public String entityType() {
        return "city";
    }

    @Override
    public @Nullable Object state(ToolContext context, CreateCityArguments a) {
        return null;
    }

    @Override
    public Written write(ToolContext context, CreateCityArguments a) {
        PlaceRequestDto request = new PlaceRequestDto(a.names().byLanguage(), a.countryId(), null, null);
        return Written.of(
                handler.handle(mapper.toCreateCity(context.actor(), request)).value());
    }
}
