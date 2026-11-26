package com.archive.backend.vote.app;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.vote.read.MyVote;
import com.archive.backend.vote.read.SoftwareVoteReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListMyVotesHandler {

    /** More than the largest page of software any list shows. */
    private static final int MOST_SOFTWARE = 100;

    private final SoftwareVoteReadRepository reads;

    /** A public visitor has no votes; software the reader did not vote on, or that does not exist, is left out. */
    @Transactional(readOnly = true)
    public List<MyVote> handle(ListMyVotesQuery q) {
        if (q.software().size() > MOST_SOFTWARE) {
            throw new InvalidValueException(
                    "vote.too-much-software", "Ask for the votes on at most " + MOST_SOFTWARE + " software at once.");
        }
        UserId reader = q.actor().id();
        if (reader == null || q.software().isEmpty()) {
            return List.of();
        }
        return reads.votesBy(
                reader.value(), q.software().stream().map(SoftwareId::value).toList());
    }
}
