package com.archive.backend.assistant.domain.skill;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** The name of a skill of the assistant, as the model asks for it. */
@ValueObject
public record SkillName(String value) {

    public SkillName {
        Objects.requireNonNull(value, "value");
    }
}
