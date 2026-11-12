package com.archive.backend.search.web.mapper;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.FullSearchQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundGroup;
import com.archive.backend.search.web.dto.FoundAuthorDto;
import com.archive.backend.search.web.dto.FoundGroupDto;
import com.archive.backend.search.web.dto.FoundPageDto;
import com.archive.backend.search.web.dto.PageMetadataDto;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring")
public interface FullSearchWebMapper {

    FoundAuthorDto toAuthor(FoundAuthor author);

    FoundGroupDto toGroup(FoundGroup group);

    default FullSearchQuery toQuery(@Nullable String keyword, @Nullable String platform, Pageable pageable) {
        return new FullSearchQuery(
                CatalogKeyword.of(keyword), platform == null ? null : new PlatformCode(platform), pageable);
    }

    default FoundPageDto<FoundAuthorDto> toAuthors(Page<FoundAuthor> page) {
        return toPage(page, this::toAuthor);
    }

    default FoundPageDto<FoundGroupDto> toGroups(Page<FoundGroup> page) {
        return toPage(page, this::toGroup);
    }

    private static <T, D> FoundPageDto<D> toPage(Page<T> page, Function<T, D> map) {
        return new FoundPageDto<>(
                page.getContent().stream().map(map).toList(),
                new PageMetadataDto(page.getSize(), page.getNumber(), page.getTotalElements(), page.getTotalPages()));
    }
}
