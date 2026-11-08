package com.archive.backend.software.app;

import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class InMemorySoftwareRepository implements SoftwareRepository {

    private final Map<SoftwareId, Software> items = new LinkedHashMap<>();

    @Override
    public Optional<Software> findById(SoftwareId id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public void save(Software software) {
        items.put(software.getId(), software);
    }

    @Override
    public void delete(Software software) {
        items.remove(software.getId());
    }
}
