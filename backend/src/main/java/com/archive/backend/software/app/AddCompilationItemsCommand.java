package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import java.util.Set;

public record AddCompilationItemsCommand(Actor actor, SoftwareId compilation, Set<SoftwareId> programs) {}
