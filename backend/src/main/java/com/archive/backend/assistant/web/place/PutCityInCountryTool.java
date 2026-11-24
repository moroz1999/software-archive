package com.archive.backend.assistant.web.place;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.geo.app.GetCityHandler;
import com.archive.backend.geo.app.GetCityQuery;
import com.archive.backend.geo.app.PutCityInCountryHandler;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.read.CityDetails;
import com.archive.backend.geo.web.dto.CityCountryRequestDto;
import com.archive.backend.geo.web.mapper.PlaceWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Puts a city in a country; the places in it follow it. */
@Component
@RequiredArgsConstructor
public class PutCityInCountryTool implements WriteTool<PutCityInCountryArguments> {

    private final GetCityHandler city;
    private final PutCityInCountryHandler handler;
    private final PlaceWebMapper mapper;

    @Override
    public String name() {
        return "put_city_in_country";
    }

    @Override
    public String description() {
        return "Puts a city in a country: a city of no known country once its country is known, or a city put in a wrong"
                + " one. Every place in the city follows it.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid", "description": "The city."},
                    "countryId": {"type": "string", "format": "uuid"}
                  },
                  "required": ["id", "countryId"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<PutCityInCountryArguments> argumentsType() {
        return PutCityInCountryArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.UPDATE;
    }

    @Override
    public String entityType() {
        return "city";
    }

    @Override
    public CityDetails state(ToolContext context, PutCityInCountryArguments a) {
        return city.handle(new GetCityQuery(new CityId(a.id())));
    }

    @Override
    public Written write(ToolContext context, PutCityInCountryArguments a) {
        handler.handle(mapper.toPutInCountry(context.actor(), a.id(), new CityCountryRequestDto(a.countryId())));
        return Written.of(a.id());
    }
}
