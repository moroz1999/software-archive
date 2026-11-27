package com.archive.backend.importer.web.mapper;

import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.read.ImportRunAddedEntry;
import com.archive.backend.importer.read.ImportRunAuthorEntry;
import com.archive.backend.importer.read.ImportRunDownloadedEntry;
import com.archive.backend.importer.read.ImportRunFailedEntry;
import com.archive.backend.importer.read.ImportRunKnownEntry;
import com.archive.backend.importer.read.ImportRunListReadEntry;
import com.archive.backend.importer.read.ImportRunLogLine;
import com.archive.backend.importer.read.ImportRunPlaceEntry;
import com.archive.backend.importer.read.ImportRunProgressEntry;
import com.archive.backend.importer.read.ImportRunReport;
import com.archive.backend.importer.read.ImportRunSkippedEntry;
import com.archive.backend.importer.web.dto.ImportRunAddedDto;
import com.archive.backend.importer.web.dto.ImportRunAuthorDto;
import com.archive.backend.importer.web.dto.ImportRunDownloadedDto;
import com.archive.backend.importer.web.dto.ImportRunDto;
import com.archive.backend.importer.web.dto.ImportRunFailedDto;
import com.archive.backend.importer.web.dto.ImportRunKnownDto;
import com.archive.backend.importer.web.dto.ImportRunListReadDto;
import com.archive.backend.importer.web.dto.ImportRunLogLineDto;
import com.archive.backend.importer.web.dto.ImportRunPlaceDto;
import com.archive.backend.importer.web.dto.ImportRunProgressDto;
import com.archive.backend.importer.web.dto.ImportRunSkippedDto;
import java.time.Instant;
import org.jspecify.annotations.Nullable;
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

    ImportRunListReadDto toDto(ImportRunListReadEntry entry);

    ImportRunDownloadedDto toDto(ImportRunDownloadedEntry entry);

    /** A line of the log with what it tells in the shape of its kind. */
    default ImportRunLogLineDto toDto(ImportRunLogLine line) {
        long seq = line.seq();
        @Nullable Instant at = line.at();
        return switch (line.entry()) {
            case ImportRunListReadEntry e -> new ImportRunLogLineDto.ListRead(seq, at, toDto(e));
            case ImportRunDownloadedEntry e -> new ImportRunLogLineDto.Downloaded(seq, at, toDto(e));
            case ImportRunAddedEntry e -> new ImportRunLogLineDto.Added(seq, at, toDto(e));
            case ImportRunKnownEntry e -> new ImportRunLogLineDto.Known(seq, at, toDto(e));
            case ImportRunSkippedEntry e -> new ImportRunLogLineDto.Skipped(seq, at, toDto(e));
            case ImportRunFailedEntry e -> new ImportRunLogLineDto.Failed(seq, at, toDto(e));
            case ImportRunAuthorEntry e -> new ImportRunLogLineDto.Author(seq, at, toDto(e));
            case ImportRunPlaceEntry e -> new ImportRunLogLineDto.Place(seq, at, toDto(e));
        };
    }

    @Named("state")
    default String toState(String state) {
        return ImportRunState.valueOf(state).code();
    }
}
