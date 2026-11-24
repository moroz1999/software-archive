package com.archive.backend.assistant.web.place;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.geo.app.GetCityHandler;
import com.archive.backend.geo.app.GetCityQuery;
import com.archive.backend.geo.app.MergeCityHandler;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.read.CityDetails;
import com.archive.backend.geo.web.dto.MergeRequestDto;
import com.archive.backend.geo.web.mapper.PlaceWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Merges a city into the one it duplicates; the plan shows the city merged. */
@Component
@RequiredArgsConstructor
public class MergeCityTool implements WriteTool<MergePlaceArguments> {

    private final GetCityHandler city;
    private final MergeCityHandler handler;
    private final PlaceWebMapper mapper;

    @Override
    public String name() {
        return "merge_city";
    }

    @Override
    public String description() {
        return "Merges a city into another one it duplicates: every place in it goes to the other one and its country, its"
                + " names and coordinates go with it.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid", "description": "The city merged."},
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
        return "city";
    }

    @Override
    public CityDetails state(ToolContext context, MergePlaceArguments a) {
        return city.handle(new GetCityQuery(new CityId(a.id())));
    }

    @Override
    public Written write(ToolContext context, MergePlaceArguments a) {
        handler.handle(mapper.toMergeCity(context.actor(), a.id(), new MergeRequestDto(a.intoId())));
        return Written.of(a.intoId());
    }
}
