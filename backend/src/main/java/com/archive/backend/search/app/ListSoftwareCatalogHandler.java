package com.archive.backend.search.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.hardware.read.HardwareItem;
import com.archive.backend.hardware.read.HardwareReadRepository;
import com.archive.backend.search.read.CodeCount;
import com.archive.backend.search.read.HardwareCount;
import com.archive.backend.search.read.SoftwareCatalog;
import com.archive.backend.search.read.SoftwareCatalogAnswer;
import com.archive.backend.search.read.SoftwareCatalogFacets;
import com.archive.backend.search.read.SoftwareCatalogFilter;
import com.archive.backend.search.read.SoftwareCatalogPage;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A page of the software catalog with the values of its filters (docs/domain/search.md#software-catalog). The index
 * knows the hardware by its codes only: the reference book tells the group each chosen item is filtered in, and names
 * every item offered.
 */
@Service
@RequiredArgsConstructor
public class ListSoftwareCatalogHandler {

    private final SoftwareCatalog catalog;
    private final HardwareReadRepository hardware;

    @Transactional(readOnly = true)
    public SoftwareCatalogAnswer handle(ListSoftwareCatalogQuery q) {
        List<String> chosen =
                q.hardware().stream().map(HardwareCode::value).distinct().toList();
        Map<String, HardwareItem> known = hardware
                .findByCodes(chosen, q.language().code())
                .stream()
                .collect(Collectors.toMap(HardwareItem::code, item -> item));
        // Hardware the archive does not have is on no software: nothing is found.
        SoftwareCatalogPage page = known.size() < chosen.size()
                ? new SoftwareCatalogPage(Page.empty(q.pageable()), new SoftwareCatalogFacets(List.of(), List.of()))
                : catalog.find(filter(q, known), q.pageable());
        return new SoftwareCatalogAnswer(page, named(page.facets().hardware(), chosen, q));
    }

    private static SoftwareCatalogFilter filter(ListSoftwareCatalogQuery q, Map<String, HardwareItem> known) {
        Map<String, List<String>> byGroup = known.values().stream()
                .collect(Collectors.groupingBy(
                        HardwareItem::group,
                        LinkedHashMap::new,
                        Collectors.mapping(HardwareItem::code, Collectors.toList())));
        return new SoftwareCatalogFilter(
                q.keyword() == null ? null : q.keyword().value(),
                q.platform() == null ? null : q.platform().value(),
                q.year() == null ? null : q.year().value(),
                q.category() == null ? null : q.category().value(),
                q.country() == null ? null : q.country().value(),
                q.city() == null ? null : q.city().value(),
                q.platforms().stream().map(PlatformCode::value).toList(),
                byGroup);
    }

    /**
     * The hardware offered, named in the language asked for, in the order of the reference book; an item chosen that no
     * software found has any more stays with none, so it can be taken off.
     */
    private List<HardwareCount> named(List<CodeCount> offered, List<String> chosen, ListSoftwareCatalogQuery q) {
        Map<String, Long> counts = new LinkedHashMap<>();
        offered.forEach(value -> counts.put(value.code(), value.count()));
        Set<String> codes = new LinkedHashSet<>(counts.keySet());
        codes.addAll(chosen);
        if (codes.isEmpty()) {
            return List.of();
        }
        return hardware.findByCodes(codes, q.language().code()).stream()
                .map(item ->
                        new HardwareCount(item.code(), item.name(), item.group(), counts.getOrDefault(item.code(), 0L)))
                .toList();
    }
}
