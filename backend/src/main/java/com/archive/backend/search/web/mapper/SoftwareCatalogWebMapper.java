package com.archive.backend.search.web.mapper;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.ListSoftwareCatalogQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.search.read.IdCount;
import com.archive.backend.search.read.SoftwareCatalogAnswer;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.read.SoftwareCatalogPage;
import com.archive.backend.search.read.YearCount;
import com.archive.backend.search.web.dto.FacetValueDto;
import com.archive.backend.search.web.dto.HardwareFacetValueDto;
import com.archive.backend.search.web.dto.PageMetadataDto;
import com.archive.backend.search.web.dto.SoftwareCatalogDto;
import com.archive.backend.search.web.dto.SoftwareCatalogFacetsDto;
import com.archive.backend.search.web.dto.SoftwareCatalogItemDto;
import com.archive.backend.search.web.dto.SoftwareCatalogParams;
import com.archive.backend.shared.domain.Language;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring", uses = CardScreenshotWebMapper.class)
public interface SoftwareCatalogWebMapper {

    SoftwareCatalogItemDto toItem(SoftwareCatalogItem item);

    default ListSoftwareCatalogQuery toQuery(SoftwareCatalogParams p, Language language, Pageable pageable) {
        return new ListSoftwareCatalogQuery(
                CatalogKeyword.of(p.keyword()),
                p.platform() == null ? null : new PlatformCode(p.platform()),
                p.year() == null ? null : new ReleaseYear(p.year()),
                p.category() == null ? null : new CategoryId(p.category()),
                p.country() == null ? null : new CountryId(p.country()),
                p.city() == null ? null : new CityId(p.city()),
                codes(p.platforms()).stream().map(PlatformCode::new).toList(),
                codes(p.hardware()).stream().map(HardwareCode::new).toList(),
                language,
                pageable);
    }

    /** The codes of a list given by commas; blank ones left out. */
    private static List<String> codes(@Nullable List<String> given) {
        return given == null
                ? List.of()
                : given.stream()
                        .map(String::trim)
                        .filter(code -> !code.isEmpty())
                        .toList();
    }

    default SoftwareCatalogDto toCatalog(SoftwareCatalogAnswer answer) {
        SoftwareCatalogPage catalog = answer.catalog();
        Page<SoftwareCatalogItem> items = catalog.items();
        return new SoftwareCatalogDto(
                items.getContent().stream().map(this::toItem).toList(),
                new PageMetadataDto(
                        items.getSize(), items.getNumber(), items.getTotalElements(), items.getTotalPages()),
                new SoftwareCatalogFacetsDto(
                        catalog.facets().years().stream()
                                .map(SoftwareCatalogWebMapper::toValue)
                                .toList(),
                        ids(catalog.facets().countries()),
                        ids(catalog.facets().cities()),
                        catalog.facets().platforms().stream()
                                .map(c -> new FacetValueDto<>(c.code(), c.count()))
                                .toList(),
                        answer.hardware().stream()
                                .map(h -> new HardwareFacetValueDto(
                                        h.code(), h.name(), h.group(), h.groupName(), h.count()))
                                .toList()));
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
