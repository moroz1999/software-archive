package com.archive.backend.assistant.web.place;

import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.geo.app.GetCityHandler;
import com.archive.backend.geo.app.GetCityQuery;
import com.archive.backend.geo.app.TurnCityIntoCountryCommand;
import com.archive.backend.geo.app.TurnCityIntoCountryHandler;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.read.CityDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Makes a country of a city of no known country that is a country itself. */
@Component
@RequiredArgsConstructor
public class TurnCityIntoCountryTool implements WriteTool<IdArguments> {

    private final GetCityHandler city;
    private final TurnCityIntoCountryHandler handler;

    @Override
    public String name() {
        return "turn_city_into_country";
    }

    @Override
    public String description() {
        return "Makes a country of a city of no known country that is a country itself (an import takes a place it does not"
                + " know for a city): its names and coordinates, every place in it, primary as it was. The result is the"
                + " id of the country.";
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
        return "city";
    }

    @Override
    public CityDetails state(ToolContext context, IdArguments a) {
        return city.handle(new GetCityQuery(new CityId(a.id())));
    }

    @Override
    public Written write(ToolContext context, IdArguments a) {
        return Written.of(handler.handle(new TurnCityIntoCountryCommand(context.actor(), new CityId(a.id())))
                .value());
    }
}
