package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareSummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListCompilationProgramsHandler {

    private final SoftwareReadRepository reads;

    @Transactional(readOnly = true)
    public List<SoftwareSummary> handle(ListCompilationProgramsQuery query) {
        var id = query.id().value();
        if (!reads.exists(id)) {
            throw new SoftwareNotFoundException();
        }
        return reads.findCompilationPrograms(id);
    }
}
