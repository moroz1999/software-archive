package com.archive.backend.vote.read;

import java.util.UUID;

/** The stars the reader gave the software. */
public record MyVote(UUID software, int value) {}
