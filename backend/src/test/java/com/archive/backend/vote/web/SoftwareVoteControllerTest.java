package com.archive.backend.vote.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.shared.config.WebConfig;
import com.archive.backend.shared.web.ApiExceptionHandler;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.SignInRequiredException;
import com.archive.backend.vote.app.CastVoteCommand;
import com.archive.backend.vote.app.CastVoteHandler;
import com.archive.backend.vote.app.GetSoftwareRatingHandler;
import com.archive.backend.vote.app.GetSoftwareRatingQuery;
import com.archive.backend.vote.app.ListSoftwareVotesHandler;
import com.archive.backend.vote.app.ListSoftwareVotesQuery;
import com.archive.backend.vote.domain.SoftwareRating;
import com.archive.backend.vote.domain.VoteOutcome;
import com.archive.backend.vote.domain.VoteValue;
import com.archive.backend.vote.read.SoftwareRatingDetails;
import com.archive.backend.vote.read.SoftwareVoteEntry;
import com.archive.backend.vote.web.mapper.SoftwareVoteWebMapperImpl;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SoftwareVoteController.class)
@Import({SecurityConfig.class, WebConfig.class, SoftwareVoteWebMapperImpl.class, ApiExceptionHandler.class})
class SoftwareVoteControllerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final SoftwareId ELITE = new SoftwareId(ID);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private CastVoteHandler castHandler;

    @MockitoBean
    private GetSoftwareRatingHandler ratingHandler;

    @MockitoBean
    private ListSoftwareVotesHandler votesHandler;

    private org.springframework.test.web.servlet.ResultActions vote(String body) throws Exception {
        return mvc.perform(put("/api/software/{id}/vote", ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Test
    void castsAVoteAndAnswersWithTheNewRatingAndTheVote() throws Exception {
        when(castHandler.handle(new CastVoteCommand(Actor.anonymous(), ELITE, new VoteValue(4))))
                .thenReturn(new VoteOutcome(new SoftwareRating(3.82, 1), new VoteValue(4)));

        vote("{\"value\": 4}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(3.82))
                .andExpect(jsonPath("$.votesAmount").value(1))
                .andExpect(jsonPath("$.myVote").value(4));
    }

    @Test
    void withdrawsAVote() throws Exception {
        when(castHandler.handle(new CastVoteCommand(Actor.anonymous(), ELITE, VoteValue.WITHDRAWN)))
                .thenReturn(new VoteOutcome(SoftwareRating.NONE, null));

        vote("{\"value\": 0}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.votesAmount").value(0))
                .andExpect(jsonPath("$.myVote").isEmpty());
    }

    @Test
    void answers401ToAPublicVisitor() throws Exception {
        when(castHandler.handle(any())).thenThrow(new SignInRequiredException());

        vote("{\"value\": 4}")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("auth.required"));
    }

    @Test
    void answers404ForAVoteOnUnknownSoftware() throws Exception {
        when(castHandler.handle(any())).thenThrow(new SoftwareNotFoundException());

        vote("{\"value\": 4}")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void answers400ForMoreThanFiveStars() throws Exception {
        vote("{\"value\": 6}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("vote.value-invalid"));
        verify(castHandler, never()).handle(any());
    }

    @Test
    void answers400ForAVoteWithoutStars() throws Exception {
        vote("{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("vote.value-invalid"));
        verify(castHandler, never()).handle(any());
    }

    @Test
    void returnsTheRatingAndTheVoteOfWhoeverAsks() throws Exception {
        when(ratingHandler.handle(new GetSoftwareRatingQuery(Actor.anonymous(), ELITE)))
                .thenReturn(new SoftwareRatingDetails(4.08, 3, null));

        mvc.perform(get("/api/software/{id}/rating", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4.08))
                .andExpect(jsonPath("$.votesAmount").value(3))
                .andExpect(jsonPath("$.myVote").isEmpty());
    }

    @Test
    void answers404ForTheRatingOfUnknownSoftware() throws Exception {
        when(ratingHandler.handle(any())).thenThrow(new SoftwareNotFoundException());

        mvc.perform(get("/api/software/{id}/rating", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void listsTheVotesOfSoftware() throws Exception {
        when(votesHandler.handle(new ListSoftwareVotesQuery(ELITE)))
                .thenReturn(List.of(new SoftwareVoteEntry("Zork", 5, Instant.parse("2026-11-24T10:00:00Z"))));

        mvc.perform(get("/api/software/{id}/votes", ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].voter").value("Zork"))
                .andExpect(jsonPath("$[0].value").value(5))
                .andExpect(jsonPath("$[0].castAt").value("2026-11-24T10:00:00Z"));
    }

    @Test
    void answers404ForTheVotesOfUnknownSoftware() throws Exception {
        when(votesHandler.handle(any())).thenThrow(new SoftwareNotFoundException());

        mvc.perform(get("/api/software/{id}/votes", ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("software.not-found"));
    }

    @Test
    void answers400ForAMalformedId() throws Exception {
        mvc.perform(get("/api/software/{id}/rating", "not-an-id")).andExpect(status().isBadRequest());
    }
}
