package com.archive.backend.vote.read;

import java.time.Instant;

/** A vote of the history of software: the name of the voter, the stars and when they were cast. */
public record SoftwareVoteEntry(String voter, int value, Instant castAt) {}
