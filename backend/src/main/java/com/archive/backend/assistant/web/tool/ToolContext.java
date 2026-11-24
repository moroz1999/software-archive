package com.archive.backend.assistant.web.tool;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;

/** Whom a tool runs for and in which language it reads names kept in several languages. */
public record ToolContext(Actor actor, Language language) {}
