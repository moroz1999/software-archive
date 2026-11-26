package com.archive.backend.vote.web.dto;

import org.jspecify.annotations.Nullable;

public record SoftwareRatingDto(
        double rating, int votesAmount, @Nullable Integer myVote) {}
