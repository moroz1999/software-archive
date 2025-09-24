package com.archive.backend.software.infra;

import com.archive.backend.software.domain.Software;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SoftwareMapper {
    Software toDomain(SoftwareEntity entity);

    SoftwareEntity toEntity(Software domain);
}
