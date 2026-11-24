package com.archive.backend.assistant.web.software;

import java.util.List;
import java.util.UUID;

/** A credit as it is: who is credited, the kind of them, the name they are shown by and their roles. */
public record CreditState(UUID id, String type, String title, List<String> roles) {}
