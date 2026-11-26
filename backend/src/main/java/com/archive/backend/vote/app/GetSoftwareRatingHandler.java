package com.archive.backend.vote.app;

import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.vote.read.SoftwareRatingDetails;
import com.archive.backend.vote.read.SoftwareVoteReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSoftwareRatingHandler {

    private final SoftwareVoteReadRepository reads;

    @Transactional(readOnly = true)
    public SoftwareRatingDetails handle(GetSoftwareRatingQuery q) {
        UserId reader = q.actor().id();
        return reads.ratingOf(q.software().value(), reader == null ? null : reader.value())
                .orElseThrow(SoftwareNotFoundException::new);
    }
}
