package com.archive.backend.label.web;

import com.archive.backend.author.app.AttachLabelToAuthorCommand;
import com.archive.backend.author.app.AttachLabelToAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.app.AttachLabelToGroupCommand;
import com.archive.backend.group.app.AttachLabelToGroupHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupTypeCode;
import com.archive.backend.label.app.CreateLabelHandler;
import com.archive.backend.label.app.DeleteLabelCommand;
import com.archive.backend.label.app.DeleteLabelHandler;
import com.archive.backend.label.app.EditLabelHandler;
import com.archive.backend.label.app.GetLabelHandler;
import com.archive.backend.label.app.GetLabelQuery;
import com.archive.backend.label.app.MakeAuthorOfLabelCommand;
import com.archive.backend.label.app.MakeAuthorOfLabelHandler;
import com.archive.backend.label.app.MakeGroupOfLabelCommand;
import com.archive.backend.label.app.MakeGroupOfLabelHandler;
import com.archive.backend.label.domain.LabelAttachment;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.web.dto.LabelCreatedDto;
import com.archive.backend.label.web.dto.LabelDetailsDto;
import com.archive.backend.label.web.dto.LabelGroupRequestDto;
import com.archive.backend.label.web.dto.LabelMadeOwnerDto;
import com.archive.backend.label.web.dto.LabelOwnerRequestDto;
import com.archive.backend.label.web.dto.LabelRequestDto;
import com.archive.backend.label.web.dto.NewLabelRequestDto;
import com.archive.backend.label.web.mapper.LabelWebMapper;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.domain.Actor;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adding a label of nobody, the page of a label, attaching a label of nobody to its owner, making a new person or group of a label
 * (docs/features/label-view.md), and editing and deleting it (docs/features/label-editing.md).
 */
@RestController
@RequestMapping("/api/labels")
@RequiredArgsConstructor
public class LabelController {

    private final CreateLabelHandler createHandler;
    private final GetLabelHandler getHandler;
    private final EditLabelHandler editHandler;
    private final DeleteLabelHandler deleteHandler;
    private final AttachLabelToAuthorHandler attachToAuthorHandler;
    private final AttachLabelToGroupHandler attachToGroupHandler;
    private final MakeAuthorOfLabelHandler makeAuthorHandler;
    private final MakeGroupOfLabelHandler makeGroupHandler;
    private final LabelWebMapper webMapper;

    @PostMapping
    public ResponseEntity<LabelCreatedDto> create(Actor actor, @RequestBody NewLabelRequestDto request) {
        LabelId id = createHandler.handle(webMapper.toCreate(actor, request));
        return ResponseEntity.created(URI.create("/api/labels/" + id.value())).body(new LabelCreatedDto(id.value()));
    }

    @GetMapping("/{id}")
    public LabelDetailsDto get(Actor actor, @PathVariable UUID id, Language language) {
        return webMapper.toDetails(getHandler.handle(new GetLabelQuery(actor, new LabelId(id), language)));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void edit(Actor actor, @PathVariable UUID id, @RequestBody LabelRequestDto request) {
        editHandler.handle(webMapper.toEdit(actor, id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Actor actor, @PathVariable UUID id) {
        deleteHandler.handle(new DeleteLabelCommand(actor, new LabelId(id)));
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

    @PostMapping("/{id}/author")
    public ResponseEntity<LabelMadeOwnerDto> makeAuthor(Actor actor, @PathVariable UUID id) {
        AuthorId author = makeAuthorHandler.handle(new MakeAuthorOfLabelCommand(actor, new LabelId(id)));
        return ResponseEntity.created(URI.create("/api/authors/" + author.value()))
                .body(new LabelMadeOwnerDto(author.value()));
    }

    @PostMapping("/{id}/group")
    public ResponseEntity<LabelMadeOwnerDto> makeGroup(
            Actor actor, @PathVariable UUID id, @RequestBody LabelGroupRequestDto request) {
        Set<GroupTypeCode> types = request.types() == null
                ? Set.of()
                : request.types().stream().map(GroupTypeCode::new).collect(Collectors.toCollection(LinkedHashSet::new));
        GroupId group = makeGroupHandler.handle(new MakeGroupOfLabelCommand(actor, new LabelId(id), types));
        return ResponseEntity.created(URI.create("/api/groups/" + group.value()))
                .body(new LabelMadeOwnerDto(group.value()));
    }
}
