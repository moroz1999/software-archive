package com.archive.backend.software.infra;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.Release;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseDescription;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.ReleaseTitle;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.ReleaseVersion;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

/**
 * Software ↔ its JPA entity. Hardware sets are restored without the reference book: they were checked when stored, and
 * every set of the software is on its platform. The categories come restored by the repository, with their lines as
 * the tree has them now.
 */
@Mapper(componentModel = "spring")
public interface SoftwareMapper {

    default Software toDomain(SoftwareEntity entity, CategorySet categories) {
        PlatformCode platform = new PlatformCode(entity.getPlatformCode());
        String description = entity.getDescription();
        return new Software(
                new SoftwareId(entity.getId()),
                new SoftwareTitle(entity.getTitle()),
                description == null ? null : new SoftwareDescription(description),
                entity.getReleaseDate(),
                toHardware(platform, entity.getHardware()),
                categories,
                entity.getReleases().stream()
                        .map(release -> toRelease(platform, release))
                        .toList());
    }

    default SoftwareEntity toEntity(Software software) {
        SoftwareEntity entity = new SoftwareEntity();
        entity.setId(software.getId().value());
        entity.setTitle(software.getTitle().value());
        SoftwareDescription description = software.getDescription();
        entity.setDescription(description == null ? null : description.value());
        entity.setReleaseDate(software.getReleaseDate());
        entity.setPlatformCode(software.getPlatform().value());
        entity.setHardware(fromHardware(software.getHardware()));
        entity.setCategories(
                software.getCategories().ids().stream().map(CategoryId::value).collect(Collectors.toSet()));
        entity.setReleases(software.getReleases().stream()
                .map(SoftwareMapper::fromRelease)
                .collect(Collectors.toCollection(ArrayList::new)));
        return entity;
    }

    private static HardwareSet toHardware(PlatformCode platform, Set<String> codes) {
        return HardwareSet.restore(
                platform, codes.stream().map(HardwareCode::new).collect(Collectors.toSet()));
    }

    private static Set<String> fromHardware(HardwareSet hardware) {
        return hardware.codes().stream().map(HardwareCode::value).collect(Collectors.toSet());
    }

    private static Release toRelease(PlatformCode platform, SoftwareReleaseEntity entity) {
        String title = entity.getTitle();
        String version = entity.getVersion();
        String description = entity.getDescription();
        return new Release(
                new ReleaseId(entity.getId()),
                new ReleaseContent(
                        title == null ? null : new ReleaseTitle(title),
                        version == null ? null : new ReleaseVersion(version),
                        ReleaseType.fromCode(entity.getType()),
                        entity.getReleaseDate(),
                        description == null ? null : new ReleaseDescription(description)),
                toHardware(platform, entity.getHardware()));
    }

    private static SoftwareReleaseEntity fromRelease(Release release) {
        ReleaseContent content = release.content();
        ReleaseTitle title = content.title();
        ReleaseVersion version = content.version();
        ReleaseDescription description = content.description();
        SoftwareReleaseEntity entity = new SoftwareReleaseEntity();
        entity.setId(release.id().value());
        entity.setTitle(title == null ? null : title.value());
        entity.setVersion(version == null ? null : version.value());
        entity.setType(content.type().code());
        entity.setReleaseDate(content.releaseDate());
        entity.setDescription(description == null ? null : description.value());
        entity.setHardware(fromHardware(release.hardware()));
        return entity;
    }
}
