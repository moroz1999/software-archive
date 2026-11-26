package com.archive.backend.vote.infra;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.ADMIN_ID;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.OTHER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.archive.backend.user.TestActors;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.session.AccessTokens;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** Voting on software through the REST API, stored and read back (docs/features/voting.md). */
@IntegrationTest
@Transactional
@Rollback
class SoftwareVotingIT {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-00e7-000000000001");
    private static final UUID UNKNOWN = UUID.fromString("00000000-0000-0000-00e7-000000000099");
    private static final double AVERAGE = 3.8;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private AccessTokens accessTokens;

    @BeforeEach
    void insert() {
        TestActors.store(jdbc);
        jdbc.update(
                "INSERT INTO software (id, title, platform_code, added_by) VALUES (?, 'Elite', 'vector', ?)",
                ELITE,
                ADMIN_ID.value());
    }

    private ResultActions vote(Actor actor, int value) throws Exception {
        return mvc.perform(put("/api/software/{id}/vote", ELITE)
                .with(TestActors.bearer(accessTokens, actor))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": " + value + "}"));
    }

    private ResultActions rating(Actor actor) throws Exception {
        return mvc.perform(get("/api/software/{id}/rating", ELITE).with(TestActors.bearer(accessTokens, actor)));
    }

    /** The rating of the votes, none of them dropped: (sum + 3.8 × 10) / (n + 10). */
    private static double weighted(int... votes) {
        int sum = 0;
        for (int v : votes) {
            sum += v;
        }
        return (sum + AVERAGE * 10) / (votes.length + 10);
    }

    @Test
    void softwareNobodyVotedOnHasNoRating() throws Exception {
        mvc.perform(get("/api/software/{id}/rating", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(0.0))
                .andExpect(jsonPath("$.votesAmount").value(0))
                .andExpect(jsonPath("$.myVote").isEmpty());
        mvc.perform(get("/api/software/{id}/votes", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void theVotesOfSeveralUsersMakeTheWeightedRating() throws Exception {
        vote(MEMBER, 5)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.myVote").value(5));
        vote(OTHER, 4)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", closeTo(weighted(5, 4), 1e-9)))
                .andExpect(jsonPath("$.votesAmount").value(2))
                .andExpect(jsonPath("$.myVote").value(4));

        rating(MEMBER)
                .andExpect(jsonPath("$.rating", closeTo(weighted(5, 4), 1e-9)))
                .andExpect(jsonPath("$.votesAmount").value(2))
                .andExpect(jsonPath("$.myVote").value(5));
        rating(ADMIN).andExpect(jsonPath("$.myVote").isEmpty());
        mvc.perform(get("/api/software/{id}/rating", ELITE))
                .andExpect(jsonPath("$.myVote").isEmpty());
    }

    @Test
    void aNewVoteReplacesTheEarlierOneOfTheSameUser() throws Exception {
        vote(MEMBER, 5);
        vote(MEMBER, 2)
                .andExpect(jsonPath("$.rating", closeTo(weighted(2), 1e-9)))
                .andExpect(jsonPath("$.votesAmount").value(1))
                .andExpect(jsonPath("$.myVote").value(2));

        mvc.perform(get("/api/software/{id}/votes", ELITE))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].value").value(2));
    }

    @Test
    void theHistoryNamesTheVotersNewestFirst() throws Exception {
        vote(MEMBER, 5);
        vote(OTHER, 4);

        mvc.perform(get("/api/software/{id}/votes", ELITE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].voter").value("TestOther"))
                .andExpect(jsonPath("$[0].value").value(4))
                .andExpect(jsonPath("$[0].castAt").isNotEmpty())
                .andExpect(jsonPath("$[1].voter").value("TestMember"))
                .andExpect(jsonPath("$[1].value").value(5));
    }

    @Test
    void aWithdrawnVoteIsKeptButCountsForNothingAndIsNotInTheHistory() throws Exception {
        vote(MEMBER, 5);
        vote(OTHER, 4);

        vote(MEMBER, 0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", closeTo(weighted(4), 1e-9)))
                .andExpect(jsonPath("$.votesAmount").value(1))
                .andExpect(jsonPath("$.myVote").isEmpty());

        rating(MEMBER).andExpect(jsonPath("$.myVote").isEmpty());
        mvc.perform(get("/api/software/{id}/votes", ELITE))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].voter").value("TestOther"));
        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_vote WHERE software_id = ? AND value = 0", Integer.class, ELITE))
                .isEqualTo(1);
    }

    @Test
    void aPublicVisitorCannotVote() throws Exception {
        mvc.perform(put("/api/software/{id}/vote", ELITE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": 5}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.required"));
    }

    @Test
    void unknownSoftwareIsNotFound() throws Exception {
        mvc.perform(put("/api/software/{id}/vote", UNKNOWN)
                        .with(TestActors.bearer(accessTokens, MEMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\": 5}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
        mvc.perform(get("/api/software/{id}/rating", UNKNOWN)).andExpect(status().isNotFound());
        mvc.perform(get("/api/software/{id}/votes", UNKNOWN)).andExpect(status().isNotFound());
    }

    @Test
    void deletingSoftwareDeletesItsVotes() throws Exception {
        vote(MEMBER, 5);

        mvc.perform(delete("/api/software/{id}", ELITE).with(TestActors.bearer(accessTokens, ADMIN)))
                .andExpect(status().isNoContent());

        assertThat(jdbc.queryForObject(
                        "SELECT count(*) FROM software_vote WHERE software_id = ?", Integer.class, ELITE))
                .isZero();
    }

    @Test
    void theCardsOfTheLatestSoftwareAndOfThePeopleCreditedOnItShowTheRating() throws Exception {
        UUID zed = UUID.fromString("00000000-0000-0000-00e7-000000000011");
        jdbc.update("INSERT INTO person (id, real_name) VALUES (?, 'Zed Zedson')", zed);
        jdbc.update(
                "INSERT INTO software_contribution (software_id, person_id, role_codes) VALUES (?, ?, ARRAY['code'])",
                ELITE,
                zed);
        vote(MEMBER, 5);

        mvc.perform(get("/api/software/latest").param("platform", "vector").param("size", "100"))
                .andExpect(jsonPath("$.content[?(@.id == '%s')].rating".formatted(ELITE))
                        .value(hasItem(closeTo(weighted(5), 1e-9))))
                .andExpect(jsonPath("$.content[?(@.id == '%s')].votesAmount".formatted(ELITE))
                        .value(hasItem(1)));
        mvc.perform(get("/api/authors/{id}/software", zed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating", closeTo(weighted(5), 1e-9)))
                .andExpect(jsonPath("$[0].votesAmount").value(1));
    }

    @Test
    void tellsTheReaderTheirOwnVotesOnTheSoftwareAsked() throws Exception {
        UUID exolon = UUID.fromString("00000000-0000-0000-00e7-000000000002");
        jdbc.update(
                "INSERT INTO software (id, title, platform_code, added_by) VALUES (?, 'Exolon', 'vector', ?)",
                exolon,
                ADMIN_ID.value());
        vote(MEMBER, 4);
        vote(OTHER, 2);
        mvc.perform(put("/api/software/{id}/vote", exolon)
                .with(TestActors.bearer(accessTokens, MEMBER))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"value\": 0}"));

        mvc.perform(get("/api/my-votes")
                        .param("software", ELITE + "," + exolon + "," + UNKNOWN)
                        .with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].software").value(ELITE.toString()))
                .andExpect(jsonPath("$[0].value").value(4));
    }

    @Test
    void tellsAPublicVisitorNoVotesOfTheirOwn() throws Exception {
        vote(MEMBER, 4);

        mvc.perform(get("/api/my-votes").param("software", ELITE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void refusesToTellTheVotesOnMoreSoftwareThanAPageHolds() throws Exception {
        StringBuilder ids = new StringBuilder(ELITE.toString());
        for (int i = 0; i < 100; i++) {
            ids.append(',').append(UUID.randomUUID());
        }

        mvc.perform(get("/api/my-votes")
                        .param("software", ids.toString())
                        .with(TestActors.bearer(accessTokens, MEMBER)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("vote.too-much-software"));
    }
}
