package com.archive.backend.assistant.web.skill;

import com.archive.backend.assistant.app.LoadSkillHandler;
import com.archive.backend.assistant.app.LoadSkillQuery;
import com.archive.backend.assistant.domain.skill.SkillName;
import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The instructions of a skill, read by the model before a task of its kind. */
@Component
@RequiredArgsConstructor
public class LoadSkillTool implements ReadTool<LoadSkillArguments> {

    private final LoadSkillHandler handler;

    @Override
    public String name() {
        return "load_skill";
    }

    @Override
    public String description() {
        return "The instructions of a skill: how a kind of task is done in this archive. Load the skill before the task.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {"name": {"type": "string", "description": "The name of the skill."}},
                  "required": ["name"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<LoadSkillArguments> argumentsType() {
        return LoadSkillArguments.class;
    }

    @Override
    public Object read(ToolContext context, LoadSkillArguments arguments) {
        return handler.handle(new LoadSkillQuery(new SkillName(arguments.name())));
    }
}
