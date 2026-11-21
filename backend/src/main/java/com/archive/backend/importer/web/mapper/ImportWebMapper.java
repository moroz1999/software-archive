package com.archive.backend.importer.web.mapper;

import com.archive.backend.importer.read.ImportRunAddedEntry;
import com.archive.backend.importer.read.ImportRunAuthorEntry;
import com.archive.backend.importer.read.ImportRunFailedEntry;
import com.archive.backend.importer.read.ImportRunKnownEntry;
import com.archive.backend.importer.read.ImportRunPlaceEntry;
import com.archive.backend.importer.read.ImportRunProgressEntry;
import com.archive.backend.importer.read.ImportRunReport;
import com.archive.backend.importer.read.ImportRunSkippedEntry;
import com.archive.backend.importer.web.dto.ImportRunAddedDto;
import com.archive.backend.importer.web.dto.ImportRunAuthorDto;
import com.archive.backend.importer.web.dto.ImportRunDto;
import com.archive.backend.importer.web.dto.ImportRunFailedDto;
import com.archive.backend.importer.web.dto.ImportRunKnownDto;
import com.archive.backend.importer.web.dto.ImportRunPlaceDto;
import com.archive.backend.importer.web.dto.ImportRunProgressDto;
import com.archive.backend.importer.web.dto.ImportRunSkippedDto;
import java.util.Locale;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ImportWebMapper {

    @Mapping(target = "state", qualifiedByName = "state")
    ImportRunDto toDto(ImportRunReport report);

    ImportRunAddedDto toDto(ImportRunAddedEntry entry);

    ImportRunFailedDto toDto(ImportRunFailedEntry entry);

    ImportRunKnownDto toDto(ImportRunKnownEntry entry);

    ImportRunSkippedDto toDto(ImportRunSkippedEntry entry);

    ImportRunAuthorDto toDto(ImportRunAuthorEntry entry);

    ImportRunPlaceDto toDto(ImportRunPlaceEntry entry);

    ImportRunProgressDto toDto(ImportRunProgressEntry entry);

    /** RUNNING → running: codes go to the frontend in lower case. */
    @Named("state")
    default String toState(String state) {
        return state.toLowerCase(Locale.ROOT);
    }
}
