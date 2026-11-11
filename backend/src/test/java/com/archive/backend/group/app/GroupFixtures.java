package com.archive.backend.group.app;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoCatalog;
import com.archive.backend.geo.domain.Locations;
import com.archive.backend.geo.domain.Places;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.group.domain.GroupTypeSet;
import com.archive.backend.group.domain.GroupTypes;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.OwnLabels;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** The group, the types and the places the group use case tests work on. */
final class GroupFixtures {

    static final GroupId CREW = new GroupId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    static final LabelId CRW = new LabelId(UUID.fromString("00000000-0000-0000-0000-000000000011"));
    static final GroupTypeCode DEMOGROUP = new GroupTypeCode("demogroup");
    static final GroupTypeCode LABEL = new GroupTypeCode("label");
    static final CountryId NORWAY = new CountryId(UUID.fromString("00000000-0000-0000-0000-000000000022"));

    private GroupFixtures() {}

    /** Crew Ltd, a demogroup known as CRW, located nowhere. */
    static Group crew() {
        return new Group(
                CREW,
                new GroupName("Crew Ltd"),
                null,
                GroupTypeSet.restore(Set.of(DEMOGROUP)),
                OwnLabels.restore(List.of(new OwnLabel(CRW, new LabelName("CRW"))), CRW),
                new Locations(List.of()));
    }

    /** Demogroup and label. */
    static GroupTypes groupTypes() {
        return new GroupTypes(codes -> Set.of(DEMOGROUP, LABEL).containsAll(codes));
    }

    /** Norway without cities. */
    static Places places() {
        return new Places(new GeoCatalog() {
            @Override
            public boolean countryExists(CountryId country) {
                return country.equals(NORWAY);
            }

            @Override
            public Optional<CountryId> countryOf(CityId city) {
                return Optional.empty();
            }
        });
    }
}
