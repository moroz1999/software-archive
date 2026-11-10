package com.archive.backend.software.infra;

import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaSoftwareRepository implements SoftwareRepository {

    private final SpringDataSoftwareRepository springDataSoftwareRepository;
    private final SoftwareMapper mapper;

    @Override
    public Optional<Software> findById(SoftwareId id) {
        return springDataSoftwareRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public void save(Software software) {
        springDataSoftwareRepository.save(mapper.toEntity(software));
    }

    @Override
    public void delete(Software software) {
        springDataSoftwareRepository.deleteById(software.getId().value());
    }
}
