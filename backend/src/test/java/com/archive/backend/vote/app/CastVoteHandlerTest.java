package com.archive.backend.vote.app;

import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.AuthenticationRequiredException;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.vote.domain.AverageVote;
import com.archive.backend.vote.domain.RatingRule;
import com.archive.backend.vote.domain.SoftwareRated;
import com.archive.backend.vote.domain.SoftwareVotes;
import com.archive.backend.vote.domain.Vote;
import com.archive.backend.vote.domain.VoteOutcome;
import com.archive.backend.vote.domain.VoteValue;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CastVoteHandlerTest {

    private static final SoftwareId ELITE = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final Instant NOW = Instant.parse("2026-11-26T10:00:00Z");

    private final RatingRule rule = new RatingRule(new AverageVote(3.8));
    private final InMemorySoftwareVotesRepository repository = new InMemorySoftwareVotesRepository();
    private final List<Object> published = new ArrayList<>();
    private final CastVoteHandler handler =
            new CastVoteHandler(repository, rule, published::add, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void storesTheVoteAndAnswersWithTheNewRating() {
        repository.addSoftware(ELITE);

        VoteOutcome outcome = handler.handle(new CastVoteCommand(MEMBER, ELITE, new VoteValue(4)));

        assertThat(outcome).isEqualTo(new VoteOutcome(rule.rate(List.of(new VoteValue(4))), new VoteValue(4)));
        assertThat(repository.saved(ELITE).map(SoftwareVotes::votes))
                .contains(List.of(new Vote(MEMBER_ID, new VoteValue(4), NOW)));
        assertThat(published).containsExactly(new SoftwareRated(ELITE));
    }

    @Test
    void asksAPublicVisitorToSignInFirst() {
        repository.addSoftware(ELITE);

        assertThatThrownBy(() -> handler.handle(new CastVoteCommand(Actor.anonymous(), ELITE, new VoteValue(4))))
                .isInstanceOfSatisfying(
                        AuthenticationRequiredException.class,
                        e -> assertThat(e.code()).isEqualTo("auth.required"));
        assertThat(repository.saved(ELITE)).isEmpty();
    }

    @Test
    void failsForSoftwareThatDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new CastVoteCommand(MEMBER, ELITE, new VoteValue(4))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }
}
