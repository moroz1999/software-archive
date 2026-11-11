package com.archive.backend.software.domain;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.HardwareUnknownException;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * Software with its releases. Its platform is the platform of its base hardware, and the hardware of every release
 * belongs to it (docs/domain/hardware.md).
 */
@Getter
public final class Software {

    private final SoftwareId id;
    private SoftwareTitle title;
    private @Nullable SoftwareDescription description;
    private @Nullable LocalDate releaseDate;
    /**
     * The base hardware the software is released for, on its platform.
     */
    private HardwareSet hardware;
    /**
     * The categories the software is in; it may be in none (docs/domain/categories.md).
     */
    private CategorySet categories;

    private final List<Release> releases;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public Software(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            List<Release> releases) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.description = description;
        this.releaseDate = releaseDate;
        this.hardware = Objects.requireNonNull(hardware, "hardware");
        this.categories = Objects.requireNonNull(categories, "categories");
        releases.forEach(release -> requireOwn(release.hardware()));
        this.releases = new ArrayList<>(releases);
    }

    public static Software create(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories) {
        Software software = new Software(id, title, description, releaseDate, hardware, categories, List.of());
        software.events.add(new SoftwareChanged(id));
        return software;
    }

    public PlatformCode getPlatform() {
        return hardware.platform();
    }

    /**
     * The platform changes only while no release has hardware: that hardware belongs to the old platform.
     */
    public void edit(
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories) {
        if (!hardware.platform().equals(getPlatform()) && releasesHaveHardware()) {
            throw new SoftwarePlatformLockedException();
        }
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.hardware = hardware;
        this.categories = categories;
        events.add(new SoftwareChanged(id));
    }

    /** Tells the software is going: its repository deletes it. */
    public void delete() {
        events.add(new SoftwareChanged(id));
    }

    /**
     * What happened to the software since it was loaded, handed over once.
     */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }

    public List<Release> getReleases() {
        return List.copyOf(releases);
    }

    public Optional<Release> release(ReleaseId releaseId) {
        return releases.stream()
                .filter(release -> release.id().equals(releaseId))
                .findFirst();
    }

    public void addRelease(ReleaseId releaseId, ReleaseContent content, HardwareSet hardware) {
        releases.add(new Release(releaseId, content, requireOwn(hardware)));
    }

    public void editRelease(ReleaseId releaseId, ReleaseContent content, HardwareSet hardware) {
        Release release = release(releaseId).orElseThrow(ReleaseNotFoundException::new);
        release.edit(content, requireOwn(hardware));
    }

    public void removeRelease(ReleaseId releaseId) {
        releases.remove(release(releaseId).orElseThrow(ReleaseNotFoundException::new));
    }

    /**
     * Hardware of a release belongs to the platform of its software.
     */
    private HardwareSet requireOwn(HardwareSet releaseHardware) {
        if (!releaseHardware.belongsTo(getPlatform())) {
            throw new HardwareUnknownException();
        }
        return releaseHardware;
    }

    private boolean releasesHaveHardware() {
        return releases.stream().anyMatch(release -> !release.hardware().isEmpty());
    }
}
