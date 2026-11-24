package com.archive.backend.assistant.domain.skill;

import com.archive.backend.shared.domain.error.NotFoundException;
import java.util.List;

/** The assistant has no skill of the name asked; the message names those it has. */
public class SkillNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 1L;

    public SkillNotFoundException(SkillName name, List<String> known) {
        super("assistant.skill-not-found", "Unknown skill " + name.value() + ". Skills: " + String.join(", ", known));
    }
}
