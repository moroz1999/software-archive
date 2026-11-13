package com.archive.backend.search.web.mapper;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.FullSearchQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.FoundKind;
import com.archive.backend.search.read.FoundAuthor;
import com.archive.backend.search.read.FoundGroup;
import com.archive.backend.search.read.FoundSet;
import com.archive.backend.search.read.FullSearchResult;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.web.dto.FoundAuthorDto;
import com.archive.backend.search.web.dto.FoundGroupDto;
import com.archive.backend.search.web.dto.FoundSetDto;
import com.archive.backend.search.web.dto.FullSearchDto;
import com.archive.backend.search.web.dto.PageMetadataDto;
import com.archive.backend.search.web.dto.SoftwareCatalogItemDto;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring", uses = CardScreenshotWebMapper.class)
public interface FullSearchWebMapper {

    FoundAuthorDto toAuthor(FoundAuthor author);

    FoundGroupDto toGroup(FoundGroup group);

    SoftwareCatalogItemDto toSoftware(SoftwareCatalogItem item);

    default FullSearchQuery toQuery(
            @Nullable String keyword, @Nullable String platform, @Nullable List<String> types, Pageable pageable) {
        return new FullSearchQuery(
                CatalogKeyword.of(keyword),
                platform == null ? null : new PlatformCode(platform),
                types == null
                        ? Set.of()
                        : types.stream().map(String::trim).map(FoundKind::of).collect(Collectors.toSet()),
                pageable);
    }

    default FullSearchDto toResult(FullSearchResult result) {
        return new FullSearchDto(
                result.sets().stream().map(this::toSet).toList(),
                new PageMetadataDto(result.size(), result.number(), result.total(), result.totalPages()));
    }

    private FoundSetDto toSet(FoundSet set) {
        List<?> items = switch (set) {
            case FoundSet.Authors authors ->
                authors.items().stream().map(this::toAuthor).toList();
            case FoundSet.Groups groups ->
                groups.items().stream().map(this::toGroup).toList();
            case FoundSet.Software software ->
                software.items().stream().map(this::toSoftware).toList();
        };
        return new FoundSetDto(set.kind().name().toLowerCase(Locale.ROOT), set.total(), items);
    }
}
