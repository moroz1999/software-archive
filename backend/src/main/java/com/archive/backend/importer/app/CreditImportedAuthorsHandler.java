package com.archive.backend.importer.app;

import com.archive.backend.author.app.CreateAuthorCommand;
import com.archive.backend.author.app.CreateAuthorHandler;
import com.archive.backend.author.app.SupplementAuthorCommand;
import com.archive.backend.author.app.SupplementAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.geo.app.FindOrAddCityCommand;
import com.archive.backend.geo.app.FindOrAddCityHandler;
import com.archive.backend.geo.app.FindOrAddCountryCommand;
import com.archive.backend.geo.app.FindOrAddCountryHandler;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoCatalog;
import com.archive.backend.geo.domain.GeoName;
import com.archive.backend.geo.domain.GeoNameRepository;
import com.archive.backend.geo.domain.Location;
import com.archive.backend.geo.domain.LocationDraft;
import com.archive.backend.geo.domain.Locations;
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
import com.archive.backend.importer.domain.source.MatchingAuthors;
import com.archive.backend.importer.domain.source.SourceAuthor;
import com.archive.backend.importer.domain.source.SourceAuthorKind;
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
 * only for one not located yet. An author the archive has nothing of from this source is looked for among its people
 * and groups by name, and created only when none is surely them. A person is credited under the nickname the source names them by, as long as that
 * label is still theirs: under which nickname a program came out is worth keeping.
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
    private final GeoNameRepository geoNames;
    private final GeoCatalog geoCatalog;
    private final MatchingAuthors matching;

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
            return creditPerson(new AuthorId(person.get()), author);
        }
        Optional<UUID> group = known(archive, ExternalKind.GROUP, author);
        if (group.isPresent()) {
            return creditGroup(new GroupId(group.get()), author);
        }
        Optional<UUID> label = known(archive, ExternalKind.LABEL, author);
        if (label.isPresent()) {
            return reference(SoftwareReferenceType.AUTHOR_LABEL, label.get());
        }
        Optional<AuthorId> match = matchingPerson(author);
        if (match.isPresent()) {
            remember(archive, ExternalKind.AUTHOR, author, match.get().value());
            return creditPerson(match.get(), author);
        }
        Optional<GroupId> matchingGroup = matchingGroup(author);
        if (matchingGroup.isPresent()) {
            remember(archive, ExternalKind.GROUP, author, matchingGroup.get().value());
            return creditGroup(matchingGroup.get(), author);
        }
        return created(archive, author);
    }

    /** A person of the archive the author is: what the source tells fills only its empty fields. */
    private SoftwareReference creditPerson(AuthorId id, SourceAuthor author) {
        boolean located = authors.findById(id)
                .map(found -> !found.getLocations().all().isEmpty())
                .orElse(false);
        supplementAuthor.handle(new SupplementAuthorCommand(
                Importer.ACTOR, id, author.realName(), located ? List.of() : locationsOf(author.place())));
        return underNickname(id, author);
    }

    /** A group of the archive the author is: what the source tells fills only its empty fields. */
    private SoftwareReference creditGroup(GroupId id, SourceAuthor author) {
        boolean located = groups.findById(id)
                .map(found -> !found.getLocations().all().isEmpty())
                .orElse(false);
        supplementGroup.handle(
                new SupplementGroupCommand(Importer.ACTOR, id, located ? List.of() : locationsOf(author.place())));
        return reference(SoftwareReferenceType.GROUP, id.value());
    }

    /**
     * The one person of the archive with the same full real name and nickname from a place that does not contradict;
     * a person only the nickname or a short name tells is never taken for one of the archive.
     */
    private Optional<AuthorId> matchingPerson(SourceAuthor author) {
        RealName realName = author.realName();
        if (author.kind() != SourceAuthorKind.PERSON || realName == null || !isFull(realName)) {
            return Optional.empty();
        }
        return theOnly(matching.people(realName, author.label()).stream()
                .filter(id -> authors.findById(id)
                        .map(found -> agree(found.getLocations(), author.place()))
                        .orElse(false))
                .toList());
    }

    /**
     * The one group of the archive of the same name, by its letters and digits, from a place that does not
     * contradict. Groups almost never share a name, so a label of a source is taken for such a group too.
     */
    private Optional<GroupId> matchingGroup(SourceAuthor author) {
        if (author.kind() == SourceAuthorKind.PERSON) {
            return Optional.empty();
        }
        return theOnly(matching.groups(author.label()).stream()
                .filter(id -> groups.findById(id)
                        .map(found -> agree(found.getLocations(), author.place()))
                        .orElse(false))
                .toList());
    }

    private static <T> Optional<T> theOnly(List<T> candidates) {
        return candidates.size() == 1 ? Optional.of(candidates.getFirst()) : Optional.empty();
    }

    /** A real name of at least two words: a first name or an initial alone tells too little. */
    private static boolean isFull(RealName realName) {
        return realName.value().trim().contains(" ");
    }

    /**
     * Whether the place of the source does not contradict where the archive has the author: either is not told, or
     * they share a city or a country. A place the archive does not know is another place.
     */
    private boolean agree(Locations ours, @Nullable SourcePlace place) {
        if (place == null || ours.all().isEmpty()) {
            return true;
        }
        GeoName name = new GeoName(place.name());
        Optional<CityId> city = Optional.empty();
        Optional<CountryId> country = Optional.empty();
        switch (place.kind()) {
            case CITY -> city = geoNames.cityNamed(name);
            case COUNTRY -> country = geoNames.countryNamed(name);
            case CITY_OR_COUNTRY -> {
                country = geoNames.countryNamed(name);
                if (country.isEmpty()) {
                    city = geoNames.cityNamed(name);
                }
            }
        }
        if (city.isPresent()) {
            country = geoCatalog.countryOf(city.get());
        }
        Optional<CityId> theirCity = city;
        // A city of no known country tells nothing of its country.
        Optional<CountryId> theirCountry = country.filter(known -> !known.equals(CountryId.UNKNOWN));
        return ours.all().stream()
                .map(Location::place)
                .anyMatch(our -> (theirCity.isPresent() && theirCity.get().equals(our.city()))
                        || (theirCountry.isPresent() && theirCountry.get().equals(our.country())));
    }

    private SoftwareReference created(String archive, SourceAuthor author) {
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
                yield underNickname(new AuthorId(created), author);
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

    /** The person's label of the nickname the source names them by; the person once that label is not theirs. */
    private SoftwareReference underNickname(AuthorId person, SourceAuthor author) {
        return authors.findById(person)
                .flatMap(found -> found.getLabels().all().stream()
                        .filter(label -> label.name()
                                .value()
                                .equalsIgnoreCase(author.label().value()))
                        .findFirst())
                .map(label ->
                        reference(SoftwareReferenceType.AUTHOR_LABEL, label.id().value()))
                .orElseGet(() -> reference(SoftwareReferenceType.AUTHOR, person.value()));
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
        GeoName name = new GeoName(place.name());
        return List.of(
                switch (place.kind()) {
                    case CITY -> city(name, place);
                    case COUNTRY -> country(name, place);
                    case CITY_OR_COUNTRY ->
                        geoNames.countryNamed(name)
                                .map(country -> new LocationDraft(country, null, true))
                                .orElseGet(() -> city(name, place));
                });
    }

    private LocationDraft city(GeoName name, SourcePlace place) {
        return new LocationDraft(null, findOrAddCity.handle(new FindOrAddCityCommand(name, place.language())), true);
    }

    private LocationDraft country(GeoName name, SourcePlace place) {
        return new LocationDraft(
                findOrAddCountry.handle(new FindOrAddCountryCommand(name, place.language())), null, true);
    }

    private static SoftwareReference reference(SoftwareReferenceType type, UUID id) {
        return new SoftwareReference(type, new SoftwareReferenceId(id));
    }
}
