package com.archive.backend.hardware.web.mapper;

import com.archive.backend.hardware.read.PlatformItem;
import com.archive.backend.hardware.web.dto.PlatformDto;
import java.util.List;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PlatformWebMapper {

    List<PlatformDto> toPlatforms(List<PlatformItem> platforms);
}
