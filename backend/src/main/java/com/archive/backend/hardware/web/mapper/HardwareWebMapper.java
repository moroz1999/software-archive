package com.archive.backend.hardware.web.mapper;

import com.archive.backend.hardware.app.CreateHardwareCommand;
import com.archive.backend.hardware.app.CreateHardwareGroupCommand;
import com.archive.backend.hardware.app.ListHardwareQuery;
import com.archive.backend.hardware.app.UpdateHardwareCommand;
import com.archive.backend.hardware.app.UpdateHardwareGroupCommand;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareGroupCode;
import com.archive.backend.hardware.domain.HardwareName;
import com.archive.backend.hardware.domain.HardwareNames;
import com.archive.backend.hardware.domain.HardwarePlatforms;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.hardware.read.HardwareDetails;
import com.archive.backend.hardware.read.HardwareGroupDetails;
import com.archive.backend.hardware.read.HardwareGroupItem;
import com.archive.backend.hardware.read.HardwareItem;
import com.archive.backend.hardware.web.dto.HardwareDetailsDto;
import com.archive.backend.hardware.web.dto.HardwareDto;
import com.archive.backend.hardware.web.dto.HardwareGroupDetailsDto;
import com.archive.backend.hardware.web.dto.HardwareGroupDto;
import com.archive.backend.hardware.web.dto.HardwareGroupRequestDto;
import com.archive.backend.hardware.web.dto.HardwareRequestDto;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.domain.Actor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HardwareWebMapper {

    List<HardwareDto> toDtos(List<HardwareItem> items);

    List<HardwareGroupDto> toGroupDtos(List<HardwareGroupItem> groups);

    HardwareGroupDetailsDto toDto(HardwareGroupDetails group);

    HardwareDetailsDto toDto(HardwareDetails hardware);

    default ListHardwareQuery toQuery(@Nullable String platform, Language language) {
        if (platform == null) {
            throw new InvalidValueException("platform.required", "A platform is required.");
        }
        return new ListHardwareQuery(new PlatformCode(platform), language);
    }

    default CreateHardwareGroupCommand toCreateGroup(Actor actor, HardwareGroupRequestDto request) {
        return new CreateHardwareGroupCommand(
                actor, new HardwareGroupCode(Objects.requireNonNullElse(request.code(), "")), toNames(request.names()));
    }

    default UpdateHardwareGroupCommand toUpdateGroup(Actor actor, String code, HardwareGroupRequestDto request) {
        return new UpdateHardwareGroupCommand(actor, new HardwareGroupCode(code), toNames(request.names()));
    }

    default CreateHardwareCommand toCreate(Actor actor, HardwareRequestDto request) {
        return new CreateHardwareCommand(
                actor,
                new HardwareCode(Objects.requireNonNullElse(request.code(), "")),
                toNames(request.names()),
                toGroup(request.group()),
                toPlatforms(request.platforms()));
    }

    default UpdateHardwareCommand toUpdate(Actor actor, String code, HardwareRequestDto request) {
        return new UpdateHardwareCommand(
                actor,
                new HardwareCode(code),
                toNames(request.names()),
                toGroup(request.group()),
                toPlatforms(request.platforms()));
    }

    /** A name left blank is not given. */
    private static HardwareNames toNames(@Nullable Map<String, String> values) {
        Map<Language, HardwareName> names = new HashMap<>();
        if (values != null) {
            values.forEach((code, name) -> {
                Language language = Language.known(code);
                if (name != null && !name.isBlank()) {
                    names.put(language, new HardwareName(name));
                }
            });
        }
        return new HardwareNames(names);
    }

    private static HardwareGroupCode toGroup(@Nullable String group) {
        return new HardwareGroupCode(Objects.requireNonNullElse(group, ""));
    }

    private static HardwarePlatforms toPlatforms(@Nullable List<String> platforms) {
        return new HardwarePlatforms(
                platforms == null
                        ? Set.of()
                        : platforms.stream().map(PlatformCode::new).collect(Collectors.toSet()));
    }
}
