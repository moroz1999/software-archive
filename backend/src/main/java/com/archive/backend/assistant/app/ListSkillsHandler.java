package com.archive.backend.assistant.app;

import com.archive.backend.assistant.read.Skill;
import com.archive.backend.assistant.read.SkillReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Every skill, for the system prompt to list. */
@Service
@RequiredArgsConstructor
public class ListSkillsHandler {

    private final SkillReadRepository skills;

    @Transactional(readOnly = true)
    public List<Skill> handle(ListSkillsQuery q) {
        return skills.all();
    }
}
