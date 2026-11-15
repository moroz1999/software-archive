package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.label.domain.LabelAttachment;
import com.archive.backend.user.domain.Actor;

/**
 * Gives a label of nobody to the author found out to be its owner (docs/domain/authors.md#label), as its display name
 * when the person attaching it says so.
 */
public record AttachLabelToAuthorCommand(Actor actor, LabelAttachment label, AuthorId author) {}
