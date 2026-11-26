package com.archive.backend.search.app;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import com.archive.backend.IntegrationTest;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.TypesenseProbe;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.CatalogOrder;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.software.app.CreateSoftwareCommand;
import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.vote.app.CastVoteCommand;
import com.archive.backend.vote.app.CastVoteHandler;
import com.archive.backend.vote.domain.VoteValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.typesense.TypesenseContainer;

/**
 * A vote reaches the search document of its software, whose card shows the rating and by which the catalog is sorted
 * (docs/features/voting.md). Every change commits for real; the test drives the worker.
 */
@IntegrationTest
class SoftwareRatingSearchIT {

    @Autowired
    private ProcessSearchOutboxHandler worker;

    @Autowired
    private CreateSoftwareHandler createSoftware;

    @Autowired
    private CastVoteHandler castVote;

    @Autowired
    private ListSoftwareCatalogHandler catalog;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private TypesenseContainer typesense;

    private TypesenseProbe probe;

    private final List<UUID> software = new ArrayList<>();

    @BeforeEach
    void setUp() {
        probe = new TypesenseProbe(typesense);
        TestActors.store(jdbc);
    }

    /** Puts back what the tests committed: the votes go with their software. */
    @AfterEach
    void putBack() {
        software.forEach(id -> jdbc.update("DELETE FROM software WHERE id = ?", id));
        TestActors.remove(jdbc);
        jdbc.update("DELETE FROM search_outbox");
    }

    private SoftwareId create(String title) {
        SoftwareId id = createSoftware.handle(new CreateSoftwareCommand(
                ADMIN, new SoftwareTitle(title), null, null, new PlatformCode("vector"), Set.of(), Set.of()));
        software.add(id.value());
        return id;
    }

    private void vote(Actor actor, SoftwareId id, int value) {
        castVote.handle(new CastVoteCommand(actor, id, new VoteValue(value)));
    }

    private List<SoftwareCatalogItem> byRating() {
        return catalog.handle(new ListSoftwareCatalogQuery(
                        new CatalogKeyword("zsorted"),
                        new PlatformCode("vector"),
                        null,
                        null,
                        null,
                        null,
                        List.of(),
                        List.of(),
                        Language.EN,
                        CatalogOrder.RATING,
                        PageRequest.of(0, 20)))
                .catalog()
                .items()
                .getContent();
    }

    @Test
    void aVoteGivesTheDocumentOfTheSoftwareItsRating() throws Exception {
        SoftwareId elite = create("Zrated Elite");
        worker.handle();

        vote(MEMBER, elite, 5);
        vote(OTHER, elite, 4);
        worker.handle();

        Map<String, Object> doc = probe.software(elite.value()).orElseThrow();
        assertThat(doc.get("rating"))
                .isInstanceOfSatisfying(
                        Number.class,
                        rating -> assertThat(rating.doubleValue()).isCloseTo((9 + 38) / 12.0, within(1e-6)));
        assertThat(doc).containsEntry("votes_amount", 2);
    }

    @Test
    void theCatalogByRatingPutsTheBestRatedFirstAndTheUnratedLast() throws Exception {
        // A title of its own: the software of the other test stays in the index after the database is put back.
        SoftwareId elite = create("Zsorted Elite");
        SoftwareId exolon = create("Zsorted Exolon");
        SoftwareId tetris = create("Zsorted Tetris");
        vote(MEMBER, tetris, 5);
        vote(MEMBER, elite, 2);
        worker.handle();

        List<SoftwareCatalogItem> cards = byRating();

        assertThat(cards)
                .extracting(SoftwareCatalogItem::id)
                .containsExactly(tetris.value(), elite.value(), exolon.value());
        assertThat(cards.get(0).votesAmount()).isEqualTo(1);
        assertThat(cards.get(2).votesAmount()).isZero();
    }
}
