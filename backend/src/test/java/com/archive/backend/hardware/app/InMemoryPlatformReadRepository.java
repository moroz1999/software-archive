package com.archive.backend.hardware.app;

import com.archive.backend.hardware.read.PlatformItem;
import com.archive.backend.hardware.read.PlatformReadRepository;
import java.util.ArrayList;
import java.util.List;

class InMemoryPlatformReadRepository implements PlatformReadRepository {

    private final List<PlatformItem> items = new ArrayList<>();

    void add(PlatformItem item) {
        items.add(item);
    }

    @Override
    public List<PlatformItem> findAll() {
        return List.copyOf(items);
    }
}
