package com.archive.backend.assistant.web.group;

import java.util.List;
import java.util.UUID;

/** A member as they are: the person or the label of a person, the kind of them, the name shown and their roles. */
public record MemberState(UUID id, String type, String title, List<String> roles) {}
