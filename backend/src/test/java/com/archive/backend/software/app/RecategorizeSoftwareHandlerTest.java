package com.archive.backend.software.app;

import static com.archive.backend.software.app.ReleaseFixtures.ARCADE;
import static com.archive.backend.software.app.ReleaseFixtures.ELITE;
import static com.archive.backend.software.app.ReleaseFixtures.elite;
import static com.archive.backend.software.app.ReleaseFixtures.vector;
import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.category.domain.CategoryMoved;
import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RecategorizeSoftwareHandlerTest {

    private static final SoftwareId TETRIS = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000002"));

    /** Stores like the real repository and remembers what it stored. */
    private final List<SoftwareId> stored = new ArrayList<>();

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository() {
        @Override
        public void save(Software software) {
            stored.add(software.getId());
            super.save(software);
        }
    };

    @Test
    void storesAgainTheSoftwareTheMoveBroughtUnderOneBranchOnly() {
        repository.save(elite());
        repository.save(Software.create(TETRIS, new SoftwareTitle("Tetris"), null, null, vector(), CategorySet.EMPTY));
        stored.clear();
        RecategorizeSoftwareHandler handler =
                new RecategorizeSoftwareHandler(repository, moved -> moved.equals(ARCADE) ? List.of(ELITE) : List.of());

        handler.on(new CategoryMoved(ARCADE));

        assertThat(stored).containsExactly(ELITE);
    }

    @Test
    void storesNothingWhenTheMoveBroughtNoSoftwareUnderOneBranch() {
        repository.save(elite());
        stored.clear();
        RecategorizeSoftwareHandler handler = new RecategorizeSoftwareHandler(repository, moved -> List.of());

        handler.on(new CategoryMoved(ARCADE));

        assertThat(stored).isEmpty();
    }
}
