package com.archive.backend.hardware.web.mapper;

import com.archive.backend.hardware.app.ListHardwareQuery;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.hardware.read.HardwareItem;
import com.archive.backend.hardware.web.dto.HardwareDto;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HardwareWebMapper {

    List<HardwareDto> toDtos(List<HardwareItem> items);

    default ListHardwareQuery toQuery(@Nullable String platform) {
        if (platform == null) {
            throw new InvalidValueException("platform.required", "A platform is required.");
        }
        return new ListHardwareQuery(new PlatformCode(platform));
    }
}
