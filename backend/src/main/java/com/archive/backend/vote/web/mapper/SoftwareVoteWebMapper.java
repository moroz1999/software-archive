package com.archive.backend.vote.web.mapper;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.vote.app.CastVoteCommand;
import com.archive.backend.vote.domain.VoteOutcome;
import com.archive.backend.vote.domain.VoteValue;
import com.archive.backend.vote.read.MyVote;
import com.archive.backend.vote.read.SoftwareRatingDetails;
import com.archive.backend.vote.read.SoftwareVoteEntry;
import com.archive.backend.vote.web.dto.MyVoteDto;
import com.archive.backend.vote.web.dto.SoftwareRatingDto;
import com.archive.backend.vote.web.dto.SoftwareVoteDto;
import com.archive.backend.vote.web.dto.VoteRequestDto;
import java.util.UUID;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SoftwareVoteWebMapper {

    SoftwareRatingDto toRating(SoftwareRatingDetails rating);

    SoftwareVoteDto toVote(SoftwareVoteEntry vote);

    MyVoteDto toMine(MyVote vote);

    default SoftwareRatingDto toRating(VoteOutcome outcome) {
        VoteValue mine = outcome.mine();
        return new SoftwareRatingDto(
                outcome.rating().rating(), outcome.rating().votesAmount(), mine == null ? null : mine.value());
    }

    /** A vote without stars is malformed: withdrawing one sends 0. */
    default CastVoteCommand toCast(Actor actor, UUID software, VoteRequestDto request) {
        Integer value = request.value();
        if (value == null) {
            throw new InvalidValueException("vote.value-invalid", "A vote needs its stars.");
        }
        return new CastVoteCommand(actor, new SoftwareId(software), new VoteValue(value));
    }
}
