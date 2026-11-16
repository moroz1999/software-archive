package com.archive.backend.group.infra;

import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.label.domain.LabelId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaGroupRepository implements GroupRepository {

    private final SpringDataGroupRepository springDataGroupRepository;
    private final GroupMapper mapper;

    @Override
    public Optional<Group> findById(GroupId id) {
        return springDataGroupRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Group> findByLabel(LabelId label) {
        return springDataGroupRepository.findByLabel(label.value()).map(mapper::toDomain);
    }

    @Override
    public void save(Group group) {
        GroupEntity entity = mapper.toEntity(group);
        springDataGroupRepository.findById(entity.getId()).ifPresent(stored -> keepMemberRows(stored, entity));
        springDataGroupRepository.saveAndFlush(entity);
    }

    /**
     * A member that stays keeps its row: a new row for it would be written before the old one is removed, and clash
     * with it.
     */
    private static void keepMemberRows(GroupEntity stored, GroupEntity entity) {
        Map<List<UUID>, UUID> rows = new HashMap<>();
        stored.getMembers().forEach(member -> rows.put(memberKey(member), member.getId()));
        entity.getMembers().forEach(member -> {
            UUID row = rows.get(memberKey(member));
            if (row != null) {
                member.setId(row);
            }
        });
    }

    private static List<UUID> memberKey(GroupMemberEntity member) {
        return Arrays.asList(member.getPersonId(), member.getLabelId());
    }

    @Override
    public void delete(Group group) {
        springDataGroupRepository.deleteById(group.getId().value());
        springDataGroupRepository.flush();
    }
}
