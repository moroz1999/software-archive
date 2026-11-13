package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** The screenshots of software or of a release, in their order (docs/domain/software.md#screenshots). */
@ValueObject
public record Screenshots(List<Screenshot> all) {

    public static final Screenshots EMPTY = new Screenshots(List.of());

    public Screenshots {
        all = List.copyOf(all);
    }

    /** These with the added ones after them. */
    public Screenshots plus(List<Screenshot> added) {
        return new Screenshots(Stream.concat(all.stream(), added.stream()).toList());
    }

    /** These without the screenshot; fails when there is no such screenshot. */
    public Screenshots without(ScreenshotId id) {
        if (!ids().contains(id)) {
            throw new ScreenshotNotFoundException();
        }
        return new Screenshots(
                all.stream().filter(screenshot -> !screenshot.id().equals(id)).toList());
    }

    /** These in the order given, which has to name every one of them exactly once. */
    public Screenshots reordered(List<ScreenshotId> order) {
        Map<ScreenshotId, Screenshot> byId =
                all.stream().collect(Collectors.toMap(Screenshot::id, Function.identity()));
        if (order.size() != all.size() || !new HashSet<>(order).equals(byId.keySet())) {
            throw new ScreenshotOrderOutdatedException();
        }
        return new Screenshots(order.stream().map(byId::get).toList());
    }

    public List<ScreenshotId> ids() {
        return all.stream().map(Screenshot::id).toList();
    }
}
