package com.archive.backend.category.infra;

import static com.archive.backend.category.infra.SeededTree.DEMOSCENE;
import static com.archive.backend.category.infra.SeededTree.GAMES;
import static com.archive.backend.category.infra.SeededTree.INTRO;
import static com.archive.backend.category.infra.SeededTree.INTRO_4K;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryParent;
import com.archive.backend.category.domain.CategoryTitle;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaCategoryRepository.class, CategoryMapperImpl.class})
class JpaCategoryRepositoryIT {

    private static final CategoryId NEW = new CategoryId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000051");

    @Autowired
    private JpaCategoryRepository repository;

    @Autowired
    private SpringDataCategoryRepository springDataCategoryRepository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private EntityManager entityManager;

    private Category reload(CategoryId id) {
        springDataCategoryRepository.flush();
        // Every request starts with an empty persistence context.
        entityManager.clear();
        return repository.findById(id).orElseThrow();
    }

    private int position(UUID id) {
        Integer position = jdbc.queryForObject("SELECT position FROM category WHERE id = ?", Integer.class, id);
        return position == null ? -1 : position;
    }

    private int count(String sql, Object... args) {
        Integer count = jdbc.queryForObject(sql, Integer.class, args);
        return count == null ? 0 : count;
    }

    @Test
    void storesANewCategoryLastUnderItsParent() {
        repository.save(Category.create(
                NEW, new CategoryTitle("64K intro"), CategoryParent.restore(NEW, new CategoryId(INTRO))));

        Category stored = reload(NEW);

        assertThat(stored.getTitle()).isEqualTo(new CategoryTitle("64K intro"));
        assertThat(stored.getParent()).isEqualTo(CategoryParent.restore(NEW, new CategoryId(INTRO)));
        assertThat(position(NEW.value())).isEqualTo(15);
    }

    @Test
    void storesANewCategoryLastAtTheTop() {
        repository.save(Category.create(NEW, new CategoryTitle("Hardware"), null));

        assertThat(reload(NEW).getParent()).isNull();
        assertThat(position(NEW.value())).isEqualTo(9);
    }

    @Test
    void keepsThePlaceOfACategoryEditedUnderTheSameParent() {
        CategoryId id = new CategoryId(INTRO_4K);
        Category intro = repository.findById(id).orElseThrow();

        intro.edit(new CategoryTitle("4K intros"), intro.getParent());
        repository.save(intro);

        assertThat(reload(id).getTitle()).isEqualTo(new CategoryTitle("4K intros"));
        assertThat(position(INTRO_4K)).isEqualTo(8);
    }

    @Test
    void putsAMovedCategoryLastUnderItsNewParent() {
        CategoryId id = new CategoryId(INTRO_4K);
        int last = count("SELECT count(*) FROM category WHERE parent_id = ?", DEMOSCENE);
        Category intro = repository.findById(id).orElseThrow();

        intro.edit(intro.getTitle(), CategoryParent.restore(id, new CategoryId(DEMOSCENE)));
        repository.save(intro);

        assertThat(reload(id).getParent()).isEqualTo(CategoryParent.restore(id, new CategoryId(DEMOSCENE)));
        assertThat(position(INTRO_4K)).isEqualTo(last);
    }

    @Test
    void deletesTheCategoryWithItsSubtreeAndUnlinksItsSoftware() {
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector')", ELITE);
        jdbc.update(
                "INSERT INTO software_category (software_id, category_id) VALUES (?, ?), (?, ?), (?, ?)",
                ELITE,
                INTRO,
                ELITE,
                INTRO_4K,
                ELITE,
                GAMES);

        repository.delete(repository.findById(new CategoryId(INTRO)).orElseThrow());
        springDataCategoryRepository.flush();

        assertThat(count("SELECT count(*) FROM category WHERE id IN (?, ?)", INTRO, INTRO_4K))
                .isZero();
        assertThat(count("SELECT count(*) FROM category WHERE parent_id = ?", INTRO))
                .isZero();
        assertThat(count("SELECT count(*) FROM category WHERE id = ?", DEMOSCENE))
                .isOne();
        assertThat(count("SELECT count(*) FROM software WHERE id = ?", ELITE)).isOne();
        assertThat(count("SELECT count(*) FROM software_category WHERE software_id = ?", ELITE))
                .isOne();
        assertThat(count("SELECT count(*) FROM software_category WHERE category_id = ?", GAMES))
                .isOne();
    }

    @Test
    void unlinksTheCategoriesOfDeletedSoftware() {
        jdbc.update("INSERT INTO software (id, title, platform_code) VALUES (?, 'Elite', 'vector')", ELITE);
        jdbc.update("INSERT INTO software_category (software_id, category_id) VALUES (?, ?)", ELITE, GAMES);

        jdbc.update("DELETE FROM software WHERE id = ?", ELITE);

        assertThat(count("SELECT count(*) FROM software_category WHERE category_id = ?", GAMES))
                .isZero();
        assertThat(count("SELECT count(*) FROM category WHERE id = ?", GAMES)).isOne();
    }
}
