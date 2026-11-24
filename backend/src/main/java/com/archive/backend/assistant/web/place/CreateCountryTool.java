package com.archive.backend.assistant.web.place;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.NamesArguments;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.geo.app.CreateCountryHandler;
import com.archive.backend.geo.web.dto.PlaceRequestDto;
import com.archive.backend.geo.web.mapper.PlaceWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Adds a country, as the management of places does. */
@Component
@RequiredArgsConstructor
public class CreateCountryTool implements WriteTool<CreateCountryArguments> {

    private final CreateCountryHandler handler;
    private final PlaceWebMapper mapper;

    @Override
    public String name() {
        return "create_country";
    }

    @Override
    public String description() {
        return "Adds a country by its names. Look for it with find_places first: it may be kept under another name or as a"
                + " city of no known country.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {"names": %s},
                  "required": ["names"],
                  "additionalProperties": false
                }
                """.formatted(NamesArguments.SCHEMA);
    }

    @Override
    public Class<CreateCountryArguments> argumentsType() {
        return CreateCountryArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.CREATE;
    }

    @Override
    public String entityType() {
        return "country";
    }

    @Override
    public @Nullable Object state(ToolContext context, CreateCountryArguments a) {
        return null;
    }

    @Override
    public Written write(ToolContext context, CreateCountryArguments a) {
        PlaceRequestDto request = new PlaceRequestDto(a.names().byLanguage(), null, null, null);
        return Written.of(
                handler.handle(mapper.toCreateCountry(context.actor(), request)).value());
    }
}
