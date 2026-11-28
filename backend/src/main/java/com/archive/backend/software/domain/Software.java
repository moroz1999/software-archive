package com.archive.backend.software.domain;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.HardwareUnknownException;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.AddedBy;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    /** How it is loaded, run and played, apart from its description. */
    private @Nullable SoftwareInstructions instructions;

    private @Nullable ReleaseDate releaseDate;
    /**
     * The base hardware the software is released for, on its platform.
     */
    private HardwareSet hardware;
    /**
     * The categories the software is in; it may be in none (docs/domain/categories.md).
     */
    private CategorySet categories;

    /** Who made it, published it and worked on it (docs/domain/software.md#credits). */
    private SoftwareCredits credits;

    private SoftwareCompilationItems compilationItems;

    /** Whether the software can be had and how; unknown until it is known. */
    private PreservationStatus preservation;

    /** Where the work comes from (docs/domain/software.md#lineage); unknown until it is known. */
    private SoftwareLineage lineage;

    private final List<Release> releases;

    /**
     * The software's own pictures, gallery by gallery; the screenshots of its releases are kept by the releases
     * (docs/domain/software.md#pictures).
     */
    @Getter(lombok.AccessLevel.NONE)
    private final Map<Gallery, Screenshots> galleries = new EnumMap<>(Gallery.class);

    /** Whoever added the software may change it and all its releases (docs/domain/users.md). */
    private final AddedBy addedBy;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    @Getter(lombok.AccessLevel.NONE)
    private final Set<SoftwareUrl> homepagesToAdd = new HashSet<>();

    public Software(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareCredits credits,
            List<Release> releases,
            AddedBy addedBy,
            Map<Gallery, Screenshots> galleries,
            PreservationStatus preservation,
            @Nullable SoftwareInstructions instructions,
            SoftwareCompilationItems compilationItems,
            SoftwareLineage lineage) {
        this.id = Objects.requireNonNull(id, "id");
        this.title = Objects.requireNonNull(title, "title");
        this.description = description;
        this.releaseDate = releaseDate;
        this.hardware = Objects.requireNonNull(hardware, "hardware");
        this.categories = Objects.requireNonNull(categories, "categories");
        this.credits = Objects.requireNonNull(credits, "credits");
        releases.forEach(release -> requireOwn(release.hardware()));
        this.releases = new ArrayList<>(releases);
        this.addedBy = Objects.requireNonNull(addedBy, "addedBy");
        for (Gallery gallery : Gallery.values()) {
            this.galleries.put(gallery, galleries.getOrDefault(gallery, Screenshots.EMPTY));
        }
        this.preservation = Objects.requireNonNull(preservation, "preservation");
        this.instructions = instructions;
        compilationItems.requireOwner(id);
        this.compilationItems = compilationItems;
        this.lineage = Objects.requireNonNull(lineage, "lineage");
    }

    public Software(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareCredits credits,
            List<Release> releases,
            AddedBy addedBy,
            Map<Gallery, Screenshots> galleries,
            PreservationStatus preservation,
            @Nullable SoftwareInstructions instructions,
            SoftwareCompilationItems compilationItems) {
        this(
                id,
                title,
                description,
                releaseDate,
                hardware,
                categories,
                credits,
                releases,
                addedBy,
                galleries,
                preservation,
                instructions,
                compilationItems,
                SoftwareLineage.UNKNOWN);
    }

    public Software(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareCredits credits,
            List<Release> releases,
            AddedBy addedBy,
            Map<Gallery, Screenshots> galleries,
            PreservationStatus preservation,
            @Nullable SoftwareInstructions instructions) {
        this(
                id,
                title,
                description,
                releaseDate,
                hardware,
                categories,
                credits,
                releases,
                addedBy,
                galleries,
                preservation,
                instructions,
                new SoftwareCompilationItems(id, Set.of()));
    }

    public Software(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareCredits credits,
            List<Release> releases,
            AddedBy addedBy) {
        this(
                id,
                title,
                description,
                releaseDate,
                hardware,
                categories,
                credits,
                releases,
                addedBy,
                Map.of(),
                PreservationStatus.UNKNOWN,
                null);
    }

    public static Software create(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            HardwareSet hardware,
            CategorySet categories,
            SoftwareCredits credits,
            AddedBy addedBy) {
        Software software =
                new Software(id, title, description, releaseDate, hardware, categories, credits, List.of(), addedBy);
        software.markChanged();
        return software;
    }

    public PlatformCode getPlatform() {
        return hardware.platform();
    }

    public void changeCompilationItems(SoftwareCompilationItems items) {
        items.requireOwner(id);
        compilationItems = items;
    }

    /**
     * The platform changes only while no release has hardware: that hardware belongs to the old platform.
     */
    public void edit(
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            HardwareSet hardware,
            CategorySet categories) {
        boolean platformChanged = !hardware.platform().equals(getPlatform());
        if (platformChanged && releasesHaveHardware()) {
            throw new SoftwarePlatformLockedException();
        }
        if (platformChanged) {
            outdateFileStructures(releases.stream()
                    .filter(release -> release.archive() != null)
                    .map(Release::id)
                    .toList());
        }
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.hardware = hardware;
        this.categories = categories;
        markChanged();
    }

    /**
     * Adds what an import found and the software lacks, never rewriting what it has (docs/domain/imports.md): the
     * description, the date and the preservation status only while empty or unknown, the categories given, and the participants not credited yet. Tells what it added, for the report of the import.
     */
    public Set<SoftwareSupplement> supplement(
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            PreservationStatus preservation,
            CategorySet categories,
            SoftwareCredits credits) {
        Objects.requireNonNull(preservation, "preservation");
        Objects.requireNonNull(categories, "categories");
        Set<SoftwareSupplement> added = EnumSet.noneOf(SoftwareSupplement.class);
        if (this.description == null && description != null) {
            this.description = description;
            added.add(SoftwareSupplement.DESCRIPTION);
        }
        if (this.releaseDate == null && releaseDate != null) {
            this.releaseDate = releaseDate;
            added.add(SoftwareSupplement.RELEASE_DATE);
        }
        if (this.preservation == PreservationStatus.UNKNOWN && preservation != PreservationStatus.UNKNOWN) {
            this.preservation = preservation;
            added.add(SoftwareSupplement.PRESERVATION);
        }
        if (!categories.ids().equals(this.categories.ids())) {
            added.add(SoftwareSupplement.CATEGORIES);
        }
        this.categories = categories;
        Set<SoftwareReference> credited = this.credits.all().stream()
                .map(SoftwareCreditDraft::participant)
                .collect(Collectors.toSet());
        List<SoftwareCreditDraft> fresh = credits.all().stream()
                .filter(credit -> !credited.contains(credit.participant()))
                .toList();
        if (!fresh.isEmpty()) {
            added.add(SoftwareSupplement.CREDITS);
        }
        this.credits = SoftwareCredits.of(
                Stream.concat(this.credits.all().stream(), fresh.stream()).toList());
        markChanged();
        return added;
    }

    /** The pictures of sizes the software has no screenshot of yet, each size once (docs/domain/imports.md). */
    public List<ScreenshotImage> screenshotsOfNewSizes(List<ScreenshotImage> images) {
        Set<Long> sizes = getScreenshots().all().stream().map(Screenshot::size).collect(Collectors.toSet());
        return images.stream().filter(image -> sizes.add(image.size())).toList();
    }

    public void changeLineage(SoftwareLineage lineage) {
        this.lineage = Objects.requireNonNull(lineage, "lineage");
        markChanged();
    }

    public void changePreservation(PreservationStatus preservation) {
        this.preservation = Objects.requireNonNull(preservation, "preservation");
        markChanged();
    }

    /** Replaces everyone credited on the software. */
    public void changeCredits(SoftwareCredits credits) {
        this.credits = Objects.requireNonNull(credits, "credits");
        markChanged();
    }

    /** Tells the software is going: its repository deletes it. */
    public void delete() {
        events.add(new SoftwareChanged(id));
        events.add(new SoftwareDeleted(id));
        discard(Stream.concat(
                        galleries.values().stream().flatMap(pictures -> pictures.ids().stream()),
                        releases.stream().flatMap(release -> release.screenshots().ids().stream()))
                .toList());
        discardArchives(releases.stream()
                .map(Release::archive)
                .filter(Objects::nonNull)
                .map(ReleaseArchive::id)
                .toList());
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

    /** Anyone signed in adds releases to any software (docs/domain/users.md). Returns the release added. */
    public Release addRelease(ReleaseId releaseId, ReleaseContent content, HardwareSet hardware, AddedBy addedBy) {
        return addRelease(releaseId, content, hardware, addedBy, null);
    }

    /** Anyone signed in adds releases, optionally with their one downloadable archive. Returns the release added. */
    public Release addRelease(
            ReleaseId releaseId,
            ReleaseContent content,
            HardwareSet hardware,
            AddedBy addedBy,
            @Nullable ReleaseArchive archive) {
        return addRelease(releaseId, content, hardware, addedBy, archive, SoftwareCredits.EMPTY);
    }

    /** Anyone signed in adds releases with their credits and their archive, if any. Returns the release added. */
    public Release addRelease(
            ReleaseId releaseId,
            ReleaseContent content,
            HardwareSet hardware,
            AddedBy addedBy,
            @Nullable ReleaseArchive archive,
            SoftwareCredits credits) {
        Release release =
                new Release(releaseId, content, requireOwn(hardware), addedBy, Screenshots.EMPTY, archive, credits);
        releases.add(release);
        // The search finds software by the hardware of its releases too.
        if (!hardware.isEmpty()) {
            markChanged();
        }
        if (archive != null) {
            outdateFileStructures(List.of(releaseId));
        }
        return release;
    }

    public void editRelease(ReleaseId releaseId, ReleaseContent content, HardwareSet hardware, Actor actor) {
        editRelease(releaseId, content, hardware, actor, null, false);
    }

    /** Edits a release keeping its credits. */
    public void editRelease(
            ReleaseId releaseId,
            ReleaseContent content,
            HardwareSet hardware,
            Actor actor,
            @Nullable ReleaseArchive replacement,
            boolean removeArchive) {
        editRelease(
                releaseId, content, hardware, existingRelease(releaseId).credits(), actor, replacement, removeArchive);
    }

    /** Edits a release with its credits, replacing or removing its archive when asked. */
    public void editRelease(
            ReleaseId releaseId,
            ReleaseContent content,
            HardwareSet hardware,
            SoftwareCredits credits,
            Actor actor,
            @Nullable ReleaseArchive replacement,
            boolean removeArchive) {
        if (replacement != null && removeArchive) {
            throw new ReleaseArchiveChangeInvalidException();
        }
        Release release = release(releaseId).orElseThrow(ReleaseNotFoundException::new);
        requireReleaseChangeableBy(release, actor);
        boolean hardwareChanged = !release.hardware().equals(hardware);
        release.edit(content, requireOwn(hardware), credits);
        // The search finds software by the hardware of its releases too.
        if (hardwareChanged) {
            markChanged();
        }
        ReleaseArchive old = release.archive();
        if (replacement != null) {
            release.changeArchive(replacement);
        } else if (removeArchive) {
            release.changeArchive(null);
        }
        if (old != null && (replacement != null || removeArchive)) {
            discardArchives(List.of(old.id()));
        }
        if (replacement != null || (old != null && removeArchive)) {
            outdateFileStructures(List.of(releaseId));
        }
    }

    /** The file of a release is parsed again on request by whoever may change the release. */
    public void requireReleaseFileReparsableBy(ReleaseId releaseId, Actor actor) {
        Release release = existingRelease(releaseId);
        requireReleaseChangeableBy(release, actor);
        if (release.archive() == null) {
            throw new ReleaseArchiveNotFoundException();
        }
    }

    public void removeRelease(ReleaseId releaseId, Actor actor) {
        Release release = release(releaseId).orElseThrow(ReleaseNotFoundException::new);
        requireReleaseChangeableBy(release, actor);
        releases.remove(release);
        markChanged();
        discard(release.screenshots().ids());
        ReleaseArchive archive = release.archive();
        if (archive != null) {
            discardArchives(List.of(archive.id()));
        }
    }

    /** The software's own screenshots, as its card shows them. */
    public Screenshots getScreenshots() {
        return pictures(Gallery.SCREENSHOTS);
    }

    /** The pictures of a gallery of the software itself. */
    public Screenshots pictures(Gallery gallery) {
        return galleries.getOrDefault(gallery, Screenshots.EMPTY);
    }

    /** Adds pictures to a gallery of the software, after those there are, by whoever may change it. */
    public List<Screenshot> addPictures(Gallery gallery, List<Screenshot> added, Actor actor) {
        changePictures(gallery, actor, current -> current.plus(added));
        return List.copyOf(added);
    }

    public void removePicture(Gallery gallery, ScreenshotId id, Actor actor) {
        changePictures(gallery, actor, current -> current.without(id));
        discard(List.of(id));
    }

    /** Puts the pictures of a gallery in the order given, which names every one of them. */
    public void reorderPictures(Gallery gallery, List<ScreenshotId> order, Actor actor) {
        changePictures(gallery, actor, current -> current.reordered(order));
    }

    public void changeInstructions(@Nullable SoftwareInstructions instructions) {
        this.instructions = instructions;
    }

    public void addHomepage(SoftwareUrl url) {
        homepagesToAdd.add(Objects.requireNonNull(url, "url"));
    }

    public Set<SoftwareUrl> homepagesToAdd() {
        return Set.copyOf(homepagesToAdd);
    }

    /** The screenshots of the software itself, or of its release when one is named. */
    public Screenshots screenshots(@Nullable ReleaseId releaseId) {
        return releaseId == null ? getScreenshots() : existingRelease(releaseId).screenshots();
    }

    /**
     * Adds screenshots after those there are: to the software by whoever may change it, to a release by whoever may
     * change the release (docs/domain/users.md). Returns the screenshots added.
     */
    public List<Screenshot> addScreenshots(@Nullable ReleaseId releaseId, List<Screenshot> added, Actor actor) {
        changeScreenshots(releaseId, actor, current -> current.plus(added));
        return List.copyOf(added);
    }

    public void removeScreenshot(@Nullable ReleaseId releaseId, ScreenshotId screenshotId, Actor actor) {
        changeScreenshots(releaseId, actor, current -> current.without(screenshotId));
        discard(List.of(screenshotId));
    }

    /** Puts the screenshots in the order given, which names every one of them. */
    public void reorderScreenshots(@Nullable ReleaseId releaseId, List<ScreenshotId> order, Actor actor) {
        changeScreenshots(releaseId, actor, current -> current.reordered(order));
    }

    /**
     * Every change of screenshots changes the card: it shows those of the software, or those of its releases when it
     * has none of its own.
     */
    private void changeScreenshots(@Nullable ReleaseId releaseId, Actor actor, UnaryOperator<Screenshots> change) {
        if (releaseId == null) {
            changePictures(Gallery.SCREENSHOTS, actor, change);
            return;
        }
        Release release = existingRelease(releaseId);
        requireReleaseChangeableBy(release, actor);
        release.changeScreenshots(change.apply(release.screenshots()));
        markChanged();
    }

    /** Every change of the pictures of the software may change its card, which shows its screenshots. */
    private void changePictures(Gallery gallery, Actor actor, UnaryOperator<Screenshots> change) {
        requireChangeableBy(actor);
        galleries.put(gallery, change.apply(pictures(gallery)));
        markChanged();
    }

    private Release existingRelease(ReleaseId releaseId) {
        return release(releaseId).orElseThrow(ReleaseNotFoundException::new);
    }

    /** The pictures of the screenshots gone go too. */
    private void discard(List<ScreenshotId> ids) {
        if (!ids.isEmpty()) {
            events.add(new ScreenshotsDiscarded(ids));
        }
    }

    private void discardArchives(List<ReleaseArchiveId> ids) {
        if (!ids.isEmpty()) {
            events.add(new ReleaseArchivesDiscarded(ids));
        }
    }

    /** The parsed structure of a release follows its file and the platform that decides what is executable. */
    private void outdateFileStructures(List<ReleaseId> ids) {
        if (!ids.isEmpty()) {
            events.add(new ReleaseFileStructuresOutdated(ids));
        }
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
