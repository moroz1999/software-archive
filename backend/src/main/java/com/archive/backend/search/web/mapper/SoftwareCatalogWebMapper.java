package com.archive.backend.search.web.mapper;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.ListSoftwareCatalogQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.search.read.IdCount;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.read.SoftwareCatalogPage;
import com.archive.backend.search.read.YearCount;
import com.archive.backend.search.web.dto.FacetValueDto;
import com.archive.backend.search.web.dto.PageMetadataDto;
import com.archive.backend.search.web.dto.SoftwareCatalogDto;
import com.archive.backend.search.web.dto.SoftwareCatalogFacetsDto;
import com.archive.backend.search.web.dto.SoftwareCatalogItemDto;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring")
public interface SoftwareCatalogWebMapper {

    SoftwareCatalogItemDto toItem(SoftwareCatalogItem item);

    default ListSoftwareCatalogQuery toQuery(
            @Nullable String keyword,
            @Nullable String platform,
            @Nullable Integer year,
            @Nullable UUID category,
            @Nullable UUID country,
            Pageable pageable) {
        return new ListSoftwareCatalogQuery(
                CatalogKeyword.of(keyword),
                platform == null ? null : new PlatformCode(platform),
                year == null ? null : new ReleaseYear(year),
                category == null ? null : new CategoryId(category),
                country == null ? null : new CountryId(country),
                pageable);
    }

    default SoftwareCatalogDto toCatalog(SoftwareCatalogPage catalog) {
        Page<SoftwareCatalogItem> items = catalog.items();
        return new SoftwareCatalogDto(
                items.getContent().stream().map(this::toItem).toList(),
                new PageMetadataDto(
                        items.getSize(), items.getNumber(), items.getTotalElements(), items.getTotalPages()),
                new SoftwareCatalogFacetsDto(
                        catalog.facets().years().stream()
                                .map(SoftwareCatalogWebMapper::toValue)
                                .toList(),
                        ids(catalog.facets().categories()),
                        ids(catalog.facets().countries())));
    }

    private static FacetValueDto<Integer> toValue(YearCount year) {
        return new FacetValueDto<>(year.year(), year.count());
    }

    private static List<FacetValueDto<String>> ids(List<IdCount> counts) {
        return counts.stream()
                .map(c -> new FacetValueDto<>(c.id().toString(), c.count()))
                .toList();
    }
}
