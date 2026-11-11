package com.archive.backend.group.web;

import com.archive.backend.group.app.ListGroupTypesHandler;
import com.archive.backend.group.app.ListGroupTypesQuery;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The reference book of group types.
 */
@RestController
@RequestMapping("/api/group-types")
@RequiredArgsConstructor
public class GroupTypeController {

    private final ListGroupTypesHandler handler;

    @GetMapping
    public List<String> list() {
        return handler.handle(new ListGroupTypesQuery());
    }
}
