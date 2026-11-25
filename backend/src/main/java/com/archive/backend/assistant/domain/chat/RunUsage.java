package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** The steps a run took, each a request to the model, and the tokens they took. */
@ValueObject
public record RunUsage(int steps, long tokensIn, long tokensOut) {

    public static final RunUsage NONE = new RunUsage(0, 0, 0);

    /** One more step. */
    public RunUsage plus(long in, long out) {
        return new RunUsage(steps + 1, tokensIn + in, tokensOut + out);
    }
}
