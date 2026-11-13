package com.archive.backend.group.infra;

import com.archive.backend.author.domain.PersonReference;
import com.archive.backend.author.domain.PersonReferenceId;
import com.archive.backend.author.domain.PersonReferenceType;
import com.archive.backend.geo.infra.LocationRows;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupMemberDraft;
import com.archive.backend.group.domain.GroupMembers;
import com.archive.backend.group.domain.GroupName;
import com.archive.backend.group.domain.GroupNotes;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.group.domain.GroupTypeSet;
import com.archive.backend.label.infra.LabelRows;
import com.archive.backend.user.infra.AddedByRows;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
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
                LocationRows.toLocations(entity.getLocations()),
                GroupMembers.restore(
                        entity.getMembers().stream().map(GroupMapper::toMember).toList()),
                AddedByRows.toAddedBy(entity.getAddedBy()));
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
        entity.setAddedBy(AddedByRows.fromAddedBy(group.getAddedBy()));
        entity.setLabels(LabelRows.fromLabels(group.getLabels()));
        entity.setLocations(LocationRows.fromLocations(group.getLocations(), GroupLocationEntity::new));
        entity.setMembers(group.getMembers().all().stream()
                .map(GroupMapper::fromMember)
                .collect(Collectors.toCollection(ArrayList::new)));
        return entity;
    }

    private static GroupMemberDraft toMember(GroupMemberEntity entity) {
        UUID person = entity.getPersonId();
        return new GroupMemberDraft(
                new PersonReference(
                        person == null ? PersonReferenceType.AUTHOR_LABEL : PersonReferenceType.AUTHOR,
                        new PersonReferenceId(person == null ? Objects.requireNonNull(entity.getLabelId()) : person)),
                entity.getRoleCodes(),
                entity.getStartDate(),
                entity.getEndDate());
    }

    private static GroupMemberEntity fromMember(GroupMemberDraft member) {
        GroupMemberEntity entity = new GroupMemberEntity();
        UUID id = member.person().id().value();
        if (member.person().type() == PersonReferenceType.AUTHOR) {
            entity.setPersonId(id);
        } else {
            entity.setLabelId(id);
        }
        entity.setRoleCodes(new ArrayList<>(member.roles()));
        entity.setStartDate(member.startDate());
        entity.setEndDate(member.endDate());
        return entity;
    }
}
