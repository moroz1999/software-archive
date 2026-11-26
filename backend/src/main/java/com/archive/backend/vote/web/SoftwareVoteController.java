package com.archive.backend.vote.web;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.vote.app.CastVoteHandler;
import com.archive.backend.vote.app.GetSoftwareRatingHandler;
import com.archive.backend.vote.app.GetSoftwareRatingQuery;
import com.archive.backend.vote.app.ListSoftwareVotesHandler;
import com.archive.backend.vote.app.ListSoftwareVotesQuery;
import com.archive.backend.vote.web.dto.SoftwareRatingDto;
import com.archive.backend.vote.web.dto.SoftwareVoteDto;
import com.archive.backend.vote.web.dto.VoteRequestDto;
import com.archive.backend.vote.web.mapper.SoftwareVoteWebMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Voting on software (docs/features/voting.md#rest). */
@RestController
@RequestMapping("/api/software/{id}")
@RequiredArgsConstructor
public class SoftwareVoteController {

    private final CastVoteHandler castHandler;
    private final GetSoftwareRatingHandler ratingHandler;
    private final ListSoftwareVotesHandler votesHandler;
    private final SoftwareVoteWebMapper webMapper;

    @PutMapping("/vote")
    public SoftwareRatingDto vote(Actor actor, @PathVariable UUID id, @RequestBody VoteRequestDto request) {
        return webMapper.toRating(castHandler.handle(webMapper.toCast(actor, id, request)));
    }

    @GetMapping("/rating")
    public SoftwareRatingDto rating(Actor actor, @PathVariable UUID id) {
        return webMapper.toRating(ratingHandler.handle(new GetSoftwareRatingQuery(actor, new SoftwareId(id))));
    }

    @GetMapping("/votes")
    public List<SoftwareVoteDto> votes(@PathVariable UUID id) {
        return votesHandler.handle(new ListSoftwareVotesQuery(new SoftwareId(id))).stream()
                .map(webMapper::toVote)
                .toList();
    }
}
