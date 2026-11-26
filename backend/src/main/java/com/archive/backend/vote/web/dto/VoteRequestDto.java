package com.archive.backend.vote.web.dto;

import org.jspecify.annotations.Nullable;

/** The stars given, from 1 to 5, or 0 to withdraw the vote. */
public record VoteRequestDto(@Nullable Integer value) {}
