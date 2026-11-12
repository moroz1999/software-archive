package com.archive.backend.software.web.mapper;

import com.archive.backend.software.app.LookupSoftwareReferencesQuery;
import com.archive.backend.software.domain.SoftwareReferencePhrase;
import com.archive.backend.software.domain.SoftwareReferenceType;
import com.archive.backend.software.read.SoftwareReferenceOption;
import com.archive.backend.software.web.dto.SoftwareReferenceDto;
import com.archive.backend.software.web.dto.SoftwareReferenceLookupDto;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SoftwareReferenceWebMapper {

    default LookupSoftwareReferencesQuery toQuery(String query, String types) {
        Set<SoftwareReferenceType> parsed = Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(type -> !type.isEmpty())
                .map(SoftwareReferenceType::fromCode)
                .collect(Collectors.toUnmodifiableSet());
        return new LookupSoftwareReferencesQuery(new SoftwareReferencePhrase(query), parsed);
    }

    default SoftwareReferenceLookupDto toDto(List<SoftwareReferenceOption> options) {
        return new SoftwareReferenceLookupDto(options.stream()
                .map(option -> new SoftwareReferenceDto(option.id(), option.type(), option.title()))
                .toList());
    }
}
