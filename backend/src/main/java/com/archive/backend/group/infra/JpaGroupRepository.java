package com.archive.backend.group.infra;

import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupRepository;
import java.util.Optional;
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
    public void save(Group group) {
        springDataGroupRepository.save(mapper.toEntity(group));
    }

    @Override
    public void delete(Group group) {
        springDataGroupRepository.deleteById(group.getId().value());
    }
}
