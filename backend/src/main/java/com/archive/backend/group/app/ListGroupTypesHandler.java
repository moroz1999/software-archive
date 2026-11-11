package com.archive.backend.group.app;

import com.archive.backend.group.read.GroupTypeReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListGroupTypesHandler {

    private final GroupTypeReadRepository reads;

    @Transactional(readOnly = true)
    public List<String> handle(ListGroupTypesQuery q) {
        return reads.findAll();
    }
}
