package com.archive.backend.assistant.web.tool;

import org.jspecify.annotations.Nullable;

/**
 * A tool that changes the archive: the assistant only puts it into a plan, which runs once the editor approves it.
 * Before that, the plan shows what the change touches as it is now.
 */
public interface WriteTool<A extends Record> extends Tool<A> {

    ChangeKind kind();

    /** The type of the entry it changes: software, release, author, group or label. */
    String entityType();

    /**
     * The entry the change touches as it is now, in the names of the arguments; none for a create. An entry that
     * does not exist is refused.
     */
    @Nullable
    Object state(ToolContext context, A arguments);

    Written write(ToolContext context, A arguments);
}
