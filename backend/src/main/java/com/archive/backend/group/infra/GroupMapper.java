package com.archive.backend.group.infra;

import com.archive.backend.geo.infra.LocationRows;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupNotes;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.group.domain.GroupTypeSet;
import com.archive.backend.label.infra.LabelRows;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

/**
 * Group ↔ its JPA entity. Types, labels and places are restored without the checks: they were checked when stored.
 */
@Mapper(componentModel = "spring")
public interface GroupMapper {

    default Group toDomain(GroupEntity entity) {
        String notes = entity.getNotes();
        return new Group(
                new GroupId(entity.getId()),
                new GroupName(entity.getName()),
                notes == null ? null : new GroupNotes(notes),
                GroupTypeSet.restore(
                        entity.getTypes().stream().map(GroupTypeCode::new).collect(Collectors.toSet())),
                LabelRows.toLabels(entity.getLabels()),
                LocationRows.toLocations(entity.getLocations()));
    }

    default GroupEntity toEntity(Group group) {
        GroupNotes notes = group.getNotes();
        GroupEntity entity = new GroupEntity();
        entity.setId(group.getId().value());
        entity.setName(group.getName().value());
        entity.setNotes(notes == null ? null : notes.value());
        entity.setTypes(group.getTypes().codes().stream()
                .map(GroupTypeCode::value)
                .collect(Collectors.toCollection(HashSet::new)));
        entity.setLabels(LabelRows.fromLabels(group.getLabels()));
        entity.setLocations(LocationRows.fromLocations(group.getLocations(), GroupLocationEntity::new));
        return entity;
    }
}
