package com.archive.backend.software.infra;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SoftwareMapper {

    @Mapping(target = "platform", source = "platformCode")
    Software toDomain(SoftwareEntity entity);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "platformCode", source = "platform")
    SoftwareEntity toEntity(Software domain);

    // Value objects <-> persistence scalars.

    default @Nullable SoftwareId toId(@Nullable UUID value) {
        return value == null ? null : new SoftwareId(value);
    }

    default @Nullable UUID fromId(@Nullable SoftwareId id) {
        return id == null ? null : id.value();
    }

    default @Nullable SoftwareTitle toTitle(@Nullable String value) {
        return value == null ? null : new SoftwareTitle(value);
    }

    default @Nullable String fromTitle(@Nullable SoftwareTitle title) {
        return title == null ? null : title.value();
    }

    default @Nullable SoftwareDescription toDescription(@Nullable String value) {
        return value == null ? null : new SoftwareDescription(value);
    }

    default @Nullable String fromDescription(@Nullable SoftwareDescription description) {
        return description == null ? null : description.value();
    }

    default @Nullable PlatformCode toPlatform(@Nullable String value) {
        return value == null ? null : new PlatformCode(value);
    }

    default @Nullable String fromPlatform(@Nullable PlatformCode platform) {
        return platform == null ? null : platform.value();
    }
}
