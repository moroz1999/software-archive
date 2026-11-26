package com.archive.backend.vote.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** The settings of voting (docs/features/voting.md). */
@ConfigurationProperties("voting")
public record VotingProperties(double averageVote) {}
