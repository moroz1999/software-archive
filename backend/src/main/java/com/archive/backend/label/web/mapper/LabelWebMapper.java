package com.archive.backend.label.web.mapper;

import com.archive.backend.label.read.LabelDetails;
import com.archive.backend.label.web.dto.LabelDetailsDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabelWebMapper {

    LabelDetailsDto toDetails(LabelDetails label);
}
