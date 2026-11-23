package com.archive.backend.software.infra;

import com.archive.backend.category.domain.CategoryCatalog;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareRepository;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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
        SoftwareEntity entity = mapper.toEntity(software);
        springDataSoftwareRepository.findById(entity.getId()).ifPresent(stored -> keepCreditRows(stored, entity));
        springDataSoftwareRepository.saveAndFlush(entity);
    }

    /**
     * A credit of the same person, group or label stays its row: written anew, it would be inserted before the old row
     * is deleted, and each of them is credited once per software and once per release.
     */
    private static void keepCreditRows(SoftwareEntity stored, SoftwareEntity entity) {
        keepRows(stored.getCredits(), entity.getCredits());
        Map<UUID, List<SoftwareReleaseCreditEntity>> releases = stored.getReleases().stream()
                .collect(Collectors.toMap(SoftwareReleaseEntity::getId, SoftwareReleaseEntity::getCredits));
        entity.getReleases()
                .forEach(release -> keepRows(releases.getOrDefault(release.getId(), List.of()), release.getCredits()));
    }

    private static void keepRows(List<? extends CreditColumns> stored, List<? extends CreditColumns> written) {
        Map<UUID, UUID> rows = stored.stream()
                .collect(Collectors.toMap(
                        JpaSoftwareRepository::creditedId, credit -> Objects.requireNonNull(credit.getId())));
        written.forEach(credit -> credit.setId(rows.get(creditedId(credit))));
    }

    private static UUID creditedId(CreditColumns credit) {
        UUID person = credit.getPersonId();
        if (person != null) {
            return person;
        }
        UUID group = credit.getGroupId();
        return group == null ? Objects.requireNonNull(credit.getLabelId()) : group;
    }

    @Override
    public void delete(Software software) {
        springDataSoftwareRepository.deleteById(software.getId().value());
        springDataSoftwareRepository.flush();
    }
}
