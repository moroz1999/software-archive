package com.archive.backend.importer.app;

import com.archive.backend.author.app.CreateAuthorCommand;
import com.archive.backend.author.app.CreateAuthorHandler;
import com.archive.backend.author.app.SupplementAuthorCommand;
import com.archive.backend.author.app.SupplementAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.geo.app.FindOrAddCityCommand;
import com.archive.backend.geo.app.FindOrAddCityHandler;
import com.archive.backend.geo.app.FindOrAddCountryCommand;
import com.archive.backend.geo.app.FindOrAddCountryHandler;
import com.archive.backend.geo.domain.GeoName;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.group.app.CreateGroupCommand;
import com.archive.backend.group.app.CreateGroupHandler;
import com.archive.backend.group.app.SupplementGroupCommand;
import com.archive.backend.group.app.SupplementGroupHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.domain.ImportedEntry;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import com.archive.backend.importer.domain.Importer;
import com.archive.backend.importer.domain.source.SourceAuthor;
import com.archive.backend.importer.domain.source.SourcePlace;
import com.archive.backend.label.app.CreateLabelCommand;
import com.archive.backend.label.app.CreateLabelHandler;
import com.archive.backend.label.domain.LabelDraft;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareReferenceId;
import com.archive.backend.software.domain.SoftwareReferenceType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/**
 * Whom the authors a source names stand for in the archive (docs/domain/imports.md#origin): an author is looked for by
 * its origin first, as whatever it was made into since, and is created only when the archive has nothing of it;
 * what the source tells of an author the archive has fills only its empty fields, so a place is looked for, or added,
 * only for one not located yet.
 */
@Service
@RequiredArgsConstructor
public class CreditImportedAuthorsHandler {

    private static final List<String> UNKNOWN_ROLE = List.of("unknown");

    private final ImportedEntryRepository entries;
    private final AuthorRepository authors;
    private final GroupRepository groups;
    private final CreateLabelHandler createLabel;
    private final CreateAuthorHandler createAuthor;
    private final SupplementAuthorHandler supplementAuthor;
    private final CreateGroupHandler createGroup;
    private final SupplementGroupHandler supplementGroup;
    private final FindOrAddCityHandler findOrAddCity;
    private final FindOrAddCountryHandler findOrAddCountry;

    /** The credits of the authors of a program, each subject once, with an unknown role: no source tells roles yet. */
    public List<SoftwareCreditDraft> handle(CreditImportedAuthorsCommand c) {
        String archive = c.program().archive();
        return c.program().authors().stream()
                .map(author -> referenceOf(archive, author))
                .distinct()
                .map(reference -> new SoftwareCreditDraft(reference, UNKNOWN_ROLE))
                .toList();
    }

    private SoftwareReference referenceOf(String archive, SourceAuthor author) {
        Optional<UUID> person = known(archive, ExternalKind.AUTHOR, author);
        if (person.isPresent()) {
            AuthorId id = new AuthorId(person.get());
            boolean located = authors.findById(id)
                    .map(found -> !found.getLocations().all().isEmpty())
                    .orElse(false);
            supplementAuthor.handle(new SupplementAuthorCommand(
                    Importer.ACTOR, id, author.realName(), located ? List.of() : locationsOf(author.place())));
            return reference(SoftwareReferenceType.AUTHOR, person.get());
        }
        Optional<UUID> group = known(archive, ExternalKind.GROUP, author);
        if (group.isPresent()) {
            GroupId id = new GroupId(group.get());
            boolean located = groups.findById(id)
                    .map(found -> !found.getLocations().all().isEmpty())
                    .orElse(false);
            supplementGroup.handle(
                    new SupplementGroupCommand(Importer.ACTOR, id, located ? List.of() : locationsOf(author.place())));
            return reference(SoftwareReferenceType.GROUP, group.get());
        }
        Optional<UUID> label = known(archive, ExternalKind.LABEL, author);
        if (label.isPresent()) {
            return reference(SoftwareReferenceType.AUTHOR_LABEL, label.get());
        }
        return switch (author.kind()) {
            case LABEL -> {
                UUID created = createLabel
                        .handle(new CreateLabelCommand(Importer.ACTOR, author.label(), locationsOf(author.place())))
                        .value();
                remember(archive, ExternalKind.LABEL, author, created);
                yield reference(SoftwareReferenceType.AUTHOR_LABEL, created);
            }
            case PERSON -> {
                UUID created = createAuthor
                        .handle(new CreateAuthorCommand(
                                Importer.ACTOR,
                                author.realName(),
                                null,
                                List.of(new LabelDraft(null, author.label(), true)),
                                locationsOf(author.place())))
                        .value();
                remember(archive, ExternalKind.AUTHOR, author, created);
                yield reference(SoftwareReferenceType.AUTHOR, created);
            }
            case GROUP -> {
                UUID created = createGroup
                        .handle(new CreateGroupCommand(
                                Importer.ACTOR,
                                new GroupName(author.label().value()),
                                null,
                                Set.of(),
                                List.of(),
                                locationsOf(author.place())))
                        .value();
                remember(archive, ExternalKind.GROUP, author, created);
                yield reference(SoftwareReferenceType.GROUP, created);
            }
        };
    }

    private Optional<UUID> known(String archive, ExternalKind kind, SourceAuthor author) {
        return entries.findByOrigin(new ExternalOrigin(archive, kind, author.originId()))
                .map(entry -> entry.getEntry().value());
    }

    private void remember(String archive, ExternalKind kind, SourceAuthor author, UUID entity) {
        entries.save(
                new ImportedEntry(new ExternalOrigin(archive, kind, author.originId()), new ImportedEntryId(entity)));
    }

    private List<LocationDraft> locationsOf(@Nullable SourcePlace place) {
        if (place == null) {
            return List.of();
        }
        return List.of(
                switch (place.kind()) {
                    case CITY ->
                        new LocationDraft(
                                null,
                                findOrAddCity.handle(
                                        new FindOrAddCityCommand(new GeoName(place.name()), place.language())),
                                true);
                    case COUNTRY ->
                        new LocationDraft(
                                findOrAddCountry.handle(
                                        new FindOrAddCountryCommand(new GeoName(place.name()), place.language())),
                                null,
                                true);
                });
    }

    private static SoftwareReference reference(SoftwareReferenceType type, UUID id) {
        return new SoftwareReference(type, new SoftwareReferenceId(id));
    }
}
