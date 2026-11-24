package com.archive.backend.assistant.web.search;

import java.util.List;
import org.jspecify.annotations.Nullable;

/** The arguments of find_names. */
public record FindNamesArguments(String phrase, @Nullable List<String> types) {}
