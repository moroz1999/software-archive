package com.archive.backend.assistant.app;

import com.archive.backend.assistant.domain.skill.SkillNotFoundException;
import com.archive.backend.assistant.read.Skill;
import com.archive.backend.assistant.read.SkillReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The skill the model asks for by its name. */
@Service
@RequiredArgsConstructor
public class LoadSkillHandler {

    private final SkillReadRepository skills;

    @Transactional(readOnly = true)
    public Skill handle(LoadSkillQuery q) {
        return skills.find(q.name().value())
                .orElseThrow(() -> new SkillNotFoundException(
                        q.name(), skills.all().stream().map(Skill::name).toList()));
    }
}
