package com.archive.backend.assistant.web.tool;

/** A tool that only reads: the assistant calls it freely, without asking. Its result is serialized to JSON. */
public interface ReadTool<A extends Record> extends Tool<A> {

    Object read(ToolContext context, A arguments);
}
