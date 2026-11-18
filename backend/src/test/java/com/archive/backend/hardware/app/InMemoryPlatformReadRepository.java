package com.archive.backend.hardware.app;

import com.archive.backend.hardware.read.PlatformItem;
import com.archive.backend.hardware.read.PlatformReadRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class InMemoryPlatformReadRepository implements PlatformReadRepository {

    private final Map<String, List<PlatformItem>> items = new HashMap<>();

    void add(String language, PlatformItem item) {
        items.computeIfAbsent(language, code -> new ArrayList<>()).add(item);
    }

    @Override
    public List<PlatformItem> findAll(String language) {
        return List.copyOf(items.getOrDefault(language, List.of()));
    }
}
