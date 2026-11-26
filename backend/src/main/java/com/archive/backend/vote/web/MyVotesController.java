package com.archive.backend.vote.web;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.vote.app.ListMyVotesHandler;
import com.archive.backend.vote.app.ListMyVotesQuery;
import com.archive.backend.vote.web.dto.MyVoteDto;
import com.archive.backend.vote.web.mapper.SoftwareVoteWebMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The reader's own votes on the cards of a list (docs/features/voting.md#rest). */
@RestController
@RequiredArgsConstructor
public class MyVotesController {

    private final ListMyVotesHandler handler;
    private final SoftwareVoteWebMapper webMapper;

    @GetMapping("/api/my-votes")
    public List<MyVoteDto> mine(Actor actor, @RequestParam List<UUID> software) {
        return handler
                .handle(new ListMyVotesQuery(
                        actor, software.stream().map(SoftwareId::new).toList()))
                .stream()
                .map(webMapper::toMine)
                .toList();
    }
}
