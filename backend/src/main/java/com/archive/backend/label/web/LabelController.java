package com.archive.backend.label.web;

import com.archive.backend.author.app.AttachLabelToAuthorCommand;
import com.archive.backend.author.app.AttachLabelToAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.app.AttachLabelToGroupCommand;
import com.archive.backend.group.app.AttachLabelToGroupHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.app.GetLabelHandler;
import com.archive.backend.label.app.GetLabelQuery;
import com.archive.backend.label.domain.LabelAttachment;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.web.dto.LabelDetailsDto;
import com.archive.backend.label.web.dto.LabelOwnerRequestDto;
import com.archive.backend.label.web.mapper.LabelWebMapper;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.domain.Actor;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** The page of a label, and attaching a label of nobody to its owner (docs/features/label-view.md). */
@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {

    private final GetLabelHandler getHandler;
    private final AttachLabelToAuthorHandler attachToAuthorHandler;
    private final AttachLabelToGroupHandler attachToGroupHandler;
    private final LabelWebMapper webMapper;

    @GetMapping("/{id}")
    public LabelDetailsDto get(Actor actor, @PathVariable UUID id) {
        return webMapper.toDetails(getHandler.handle(new GetLabelQuery(actor, new LabelId(id))));
    }

    @PostMapping("/{id}/owner")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void attach(Actor actor, @PathVariable UUID id, @RequestBody LabelOwnerRequestDto request) {
        UUID owner = request.id();
        if (owner == null) {
            throw new InvalidValueException("label.owner-required", "The owner of the label is required.");
        }
        boolean display = Boolean.TRUE.equals(request.display());
        switch (String.valueOf(request.type())) {
            case "author" ->
                attachToAuthorHandler.handle(new AttachLabelToAuthorCommand(
                        actor, new LabelAttachment(new LabelId(id), display), new AuthorId(owner)));
            case "group" ->
                attachToGroupHandler.handle(new AttachLabelToGroupCommand(actor, new LabelId(id), new GroupId(owner)));
            default ->
                throw new InvalidValueException("label.owner-type-invalid", "A label belongs to a person or a group.");
        }
    }
}
