package com.archive.backend.category.infra;

import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryParent;
import com.archive.backend.category.domain.CategoryRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaCategoryRepository implements CategoryRepository {

    private final SpringDataCategoryRepository springDataCategoryRepository;
    private final CategoryMapper mapper;

    @Override
    public Optional<Category> findById(CategoryId id) {
        return springDataCategoryRepository.findById(id.value()).map(mapper::toDomain);
    }

    /**
     * A category keeps its place under the same parent; a new one, or one moved under another parent, goes last there.
     */
    @Override
    public void save(Category category) {
        CategoryParent parent = category.getParent();
        @Nullable UUID parentId = parent == null ? null : parent.id().value();
        int position = springDataCategoryRepository
                .findById(category.getId().value())
                .filter(stored -> Objects.equals(stored.getParentId(), parentId))
                .map(CategoryEntity::getPosition)
                .orElseGet(() -> springDataCategoryRepository.nextPosition(parentId));
        // Flushed at once: the tree is read by SQL in the same transaction, as by the software a move recategorizes.
        springDataCategoryRepository.saveAndFlush(mapper.toEntity(category, position));
    }

    /**
     * The schema deletes the subcategories and the links to software with the category.
     */
    @Override
    public void delete(Category category) {
        springDataCategoryRepository.deleteById(category.getId().value());
    }
}
