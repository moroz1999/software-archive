package com.archive.backend.vote.web.dto;

import java.time.Instant;

public record SoftwareVoteDto(String voter, int value, Instant castAt) {}
