package com.archive.backend.software.infra;

import com.archive.backend.category.domain.CategoryCatalog;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaSoftwareRepository implements SoftwareRepository {

    private final SpringDataSoftwareRepository springDataSoftwareRepository;
    private final SoftwareMapper mapper;
    private final CategoryCatalog categoryCatalog;

    @Override
    public Optional<Software> findById(SoftwareId id) {
        return springDataSoftwareRepository
                .findById(id.value())
                .map(entity -> mapper.toDomain(entity, categories(entity)));
    }

    /**
     * The categories of the software with their lines as the tree has them now (docs/domain/categories.md).
     */
    private CategorySet categories(SoftwareEntity entity) {
        return CategorySet.restore(categoryCatalog.linesOf(
                entity.getCategories().stream().map(CategoryId::new).collect(Collectors.toSet())));
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
