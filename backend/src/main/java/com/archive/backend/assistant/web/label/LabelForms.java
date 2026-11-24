package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.tool.LocationArguments;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.label.app.EditLabelHandler;
import com.archive.backend.label.app.GetLabelHandler;
import com.archive.backend.label.app.GetLabelQuery;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.read.LabelDetails;
import com.archive.backend.label.web.dto.LabelOwnerRequestDto;
import com.archive.backend.label.web.dto.LabelRequestDto;
import com.archive.backend.label.web.mapper.LabelWebMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** A label as its form has it: read as it is, and sent back whole with what a tool changes. */
@Component
@RequiredArgsConstructor
class LabelForms {

    private final GetLabelHandler label;
    private final EditLabelHandler edit;
    private final LabelWebMapper mapper;

    LabelState label(ToolContext context, UUID id) {
        LabelDetails details = label.handle(new GetLabelQuery(context.actor(), new LabelId(id), context.language()));
        LabelDetails.Owner owner = details.owner();
        return new LabelState(
                details.name(),
                owner == null ? null : new LabelState.Owner(owner.type(), owner.id(), owner.name()),
                details.locations().stream()
                        .map(location ->
                                new LocationArguments(location.countryId(), location.cityId(), location.primary()))
                        .toList());
    }

    /** Saves the label with this name and these locations, its owner as it is, or none when it is taken away. */
    void edit(ToolContext context, UUID id, String name, List<LocationArguments> locations, boolean keepOwner) {
        LabelState.Owner owner = keepOwner ? label(context, id).owner() : null;
        edit.handle(mapper.toEdit(
                context.actor(),
                id,
                new LabelRequestDto(
                        name,
                        owner == null ? null : new LabelOwnerRequestDto(owner.type(), owner.id(), false),
                        locations(locations))));
    }

    static @Nullable List<LabelRequestDto.LocationDto> locations(@Nullable List<LocationArguments> locations) {
        return locations == null
                ? null
                : locations.stream()
                        .map(location -> new LabelRequestDto.LocationDto(
                                location.countryId(), location.cityId(), location.primary()))
                        .toList();
    }
}
