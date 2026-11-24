package com.archive.backend.assistant.web.place;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.geo.app.GetCountryHandler;
import com.archive.backend.geo.app.GetCountryQuery;
import com.archive.backend.geo.app.MergeCountryHandler;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.read.CountryDetails;
import com.archive.backend.geo.web.dto.MergeRequestDto;
import com.archive.backend.geo.web.mapper.PlaceWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Merges a country into the one it duplicates; the plan shows the country merged. */
@Component
@RequiredArgsConstructor
public class MergeCountryTool implements WriteTool<MergePlaceArguments> {

    private final GetCountryHandler country;
    private final MergeCountryHandler handler;
    private final PlaceWebMapper mapper;

    @Override
    public String name() {
        return "merge_country";
    }

    @Override
    public String description() {
        return "Merges a country into another one it duplicates: its cities and every place in it go to the other one, its"
                + " names and coordinates go with it.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid", "description": "The country merged."},
                    "intoId": {"type": "string", "format": "uuid", "description": "The one it is merged into."}
                  },
                  "required": ["id", "intoId"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<MergePlaceArguments> argumentsType() {
        return MergePlaceArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.MERGE;
    }

    @Override
    public String entityType() {
        return "country";
    }

    @Override
    public CountryDetails state(ToolContext context, MergePlaceArguments a) {
        return country.handle(new GetCountryQuery(new CountryId(a.id())));
    }

    @Override
    public Written write(ToolContext context, MergePlaceArguments a) {
        handler.handle(mapper.toMergeCountry(context.actor(), a.id(), new MergeRequestDto(a.intoId())));
        return Written.of(a.intoId());
    }
}
