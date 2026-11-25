package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.read.model.LanguageModel;
import com.archive.backend.assistant.read.model.ModelReply;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Asks the language model. It takes part in a transaction already open but opens none: a run has none, so no
 * database connection is held while the model thinks.
 */
@Service
@RequiredArgsConstructor
public class AskModelHandler {

    private final LanguageModel model;

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ModelReply handle(AskModelQuery c) {
        return model.complete(c.request());
    }
}
