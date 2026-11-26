package com.archive.backend.vote.app;

import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.vote.read.SoftwareVoteEntry;
import com.archive.backend.vote.read.SoftwareVoteReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListSoftwareVotesHandler {

    private final SoftwareVoteReadRepository reads;

    @Transactional(readOnly = true)
    public List<SoftwareVoteEntry> handle(ListSoftwareVotesQuery q) {
        return reads.votesOf(q.software().value()).orElseThrow(SoftwareNotFoundException::new);
    }
}
