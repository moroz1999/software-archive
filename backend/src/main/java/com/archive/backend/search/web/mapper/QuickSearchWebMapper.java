package com.archive.backend.search.web.mapper;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.QuickSearchQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.read.QuickAuthor;
import com.archive.backend.search.read.QuickGroup;
import com.archive.backend.search.read.QuickSearchResult;
import com.archive.backend.search.read.QuickSection;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.web.dto.QuickAuthorDto;
import com.archive.backend.search.web.dto.QuickGroupDto;
import com.archive.backend.search.web.dto.QuickSearchDto;
import com.archive.backend.search.web.dto.QuickSectionDto;
import com.archive.backend.search.web.dto.SoftwareCatalogItemDto;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QuickSearchWebMapper {

    SoftwareCatalogItemDto toSoftware(SoftwareCatalogItem item);

    QuickAuthorDto toAuthor(QuickAuthor author);

    QuickGroupDto toGroup(QuickGroup group);

    default QuickSearchQuery toQuery(@Nullable String keyword, @Nullable String platform) {
        return new QuickSearchQuery(CatalogKeyword.of(keyword), platform == null ? null : new PlatformCode(platform));
    }

    default QuickSearchDto toResult(QuickSearchResult result) {
        return new QuickSearchDto(
                section(result.software(), this::toSoftware),
                section(result.authors(), this::toAuthor),
                section(result.groups(), this::toGroup));
    }

    private static <T, D> QuickSectionDto<D> section(QuickSection<T> section, Function<T, D> map) {
        return new QuickSectionDto<>(section.hits().stream().map(map).toList(), section.total());
    }
}
