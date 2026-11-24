package com.archive.backend.assistant.web.place;

import com.archive.backend.assistant.web.tool.NamesArguments;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of create_city: its country, none while it is not known. */
public record CreateCityArguments(
        NamesArguments names, @Nullable UUID countryId) {}
