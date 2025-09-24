package com.archive.backend.software.web.mapper;

import com.archive.backend.software.domain.Software;
import com.archive.backend.software.web.dto.SoftwareListItemDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SoftwareWebMapper {
    SoftwareListItemDto toListItem(Software domain);
}
