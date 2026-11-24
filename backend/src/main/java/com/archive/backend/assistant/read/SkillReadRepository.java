package com.archive.backend.assistant.read;

import java.util.List;
import java.util.Optional;

/** The skills of the assistant, by name. */
public interface SkillReadRepository {

    List<Skill> all();

    Optional<Skill> find(String name);
}
