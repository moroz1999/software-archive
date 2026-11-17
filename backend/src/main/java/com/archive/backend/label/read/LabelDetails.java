package com.archive.backend.label.read;

import com.archive.backend.user.read.Addition;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * The page of a label: its name, its owner once it is found out, the groups it was a member of under their main
 * names, its locations and where imports took it from (docs/domain/authors.md#label). {@code added} tells who added the
 * label and when; {@code addedBy} is whoever added it, {@code null} once their account was deleted.
 */
public record LabelDetails(
        UUID id,
        String name,
        @Nullable Owner owner,
        List<MemberOf> groups,
        List<Location> locations,
        List<Origin> origins,
        Addition added,
        @Nullable UUID addedBy,
        Rights rights) {

    /**
     * The person ({@code author}) or the group the label belongs to: a person by the real name with the main name
     * next to it ({@code note}), or by the main name without a real name; a group by its name. {@code addedBy} is
     * whoever added the owner, {@code null} once their account was deleted.
     */
    public record Owner(
            String type,
            UUID id,
            String name,
            @Nullable String note,
            @Nullable UUID addedBy) {}

    public record MemberOf(UUID id, String name) {}

    /** A place the label is located in; {@code countryId} of a city given alone is its country's. */
    public record Location(
            UUID countryId,
            String country,
            @Nullable UUID cityId,
            @Nullable String city,
            boolean primary) {}

    /** An import the label came from: the source archive, the id there and its page there, if it has one. */
    public record Origin(
            String origin, String externalId, @Nullable String url) {}

    /**
     * What the reader may do with the label: attach it to an owner ({@code canChange}), make a new owner of it, edit
     * and delete it, and say whose it is in its form.
     */
    public record Rights(boolean canChange, boolean canMakeOwner, boolean canEdit, boolean canChangeOwner) {

        public static final Rights NONE = new Rights(false, false, false, false);
    }

    public LabelDetails withRights(Rights rights) {
        return new LabelDetails(id, name, owner, groups, locations, origins, added, addedBy, rights);
    }
}
