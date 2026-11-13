package com.archive.backend.software.domain;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.HardwareUnknownException;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.AddedBy;
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
@AggregateRoot
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

    private SoftwareReferences references;
    private SoftwareCredits credits;

    private final List<Release> releases;

    /** Whoever added the software may change it and all its releases (docs/domain/users.md). */
    private final AddedBy addedBy;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public Software(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareReferences references,
            SoftwareCredits credits,
            List<Release> releases,
            AddedBy addedBy) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.description = description;
        this.releaseDate = releaseDate;
        this.hardware = Objects.requireNonNull(hardware, "hardware");
        this.categories = Objects.requireNonNull(categories, "categories");
        this.references = Objects.requireNonNull(references, "references");
        this.credits = Objects.requireNonNull(credits, "credits");
        releases.forEach(release -> requireOwn(release.hardware()));
        this.releases = new ArrayList<>(releases);
        this.addedBy = Objects.requireNonNull(addedBy, "addedBy");
    }

    public Software(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareReferences references,
            SoftwareCredits credits,
            List<Release> releases) {
        this(id, title, description, releaseDate, hardware, categories, references, credits, releases, AddedBy.NOBODY);
    }

    public Software(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareReferences references,
            List<Release> releases) {
        this(id, title, description, releaseDate, hardware, categories, references, SoftwareCredits.EMPTY, releases);
    }

    public Software(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            List<Release> releases) {
        this(
                id,
                title,
                description,
                releaseDate,
                hardware,
                categories,
                SoftwareReferences.EMPTY,
                SoftwareCredits.EMPTY,
                releases);
    }

    public static Software create(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareReferences references,
            SoftwareCredits credits,
            AddedBy addedBy) {
        Software software = new Software(
                id, title, description, releaseDate, hardware, categories, references, credits, List.of(), addedBy);
        software.markChanged();
        return software;
    }

    public static Software create(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareReferences references,
            SoftwareCredits credits) {
        return create(id, title, description, releaseDate, hardware, categories, references, credits, AddedBy.NOBODY);
    }

    public static Software create(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareReferences references) {
        return create(id, title, description, releaseDate, hardware, categories, references, SoftwareCredits.EMPTY);
    }

    public static Software create(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories) {
        return create(id, title, description, releaseDate, hardware, categories, SoftwareReferences.EMPTY);
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
            CategorySet categories,
            SoftwareReferences references) {
        if (!hardware.platform().equals(getPlatform()) && releasesHaveHardware()) {
            throw new SoftwarePlatformLockedException();
        }
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.hardware = hardware;
        this.categories = categories;
        this.references = references;
        markChanged();
    }

    public void edit(
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            HardwareSet hardware,
            CategorySet categories) {
        edit(title, description, releaseDate, hardware, categories, references);
    }

    /** Replaces all people credited by this software item. */
    public void changeCredits(SoftwareCredits credits) {
        this.credits = Objects.requireNonNull(credits, "credits");
        markChanged();
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

    /** Anyone signed in adds releases to any software (docs/domain/users.md). */
    public void addRelease(ReleaseId releaseId, ReleaseContent content, HardwareSet hardware, AddedBy addedBy) {
        releases.add(new Release(releaseId, content, requireOwn(hardware), addedBy));
    }

    public void editRelease(ReleaseId releaseId, ReleaseContent content, HardwareSet hardware, Actor actor) {
        Release release = release(releaseId).orElseThrow(ReleaseNotFoundException::new);
        requireReleaseChangeableBy(release, actor);
        release.edit(content, requireOwn(hardware));
    }

    public void removeRelease(ReleaseId releaseId, Actor actor) {
        Release release = release(releaseId).orElseThrow(ReleaseNotFoundException::new);
        requireReleaseChangeableBy(release, actor);
        releases.remove(release);
    }

    /** A release is changed by whoever added it, by whoever added its software, and by administrators. */
    private void requireReleaseChangeableBy(Release release, Actor actor) {
        actor.requireAllowed(release.addedBy().mayBeChangedBy(actor, addedBy));
    }

    /** Whoever added the software changes it, and administrators any (docs/domain/users.md). */
    public void requireChangeableBy(Actor actor) {
        addedBy.requireChangeableBy(actor);
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

    private void markChanged() {
        SoftwareChanged changed = new SoftwareChanged(id);
        if (!events.contains(changed)) {
            events.add(changed);
        }
    }
}
