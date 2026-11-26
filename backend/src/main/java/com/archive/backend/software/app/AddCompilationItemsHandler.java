package com.archive.backend.software.app;

import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareCompilationSets;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddCompilationItemsHandler {

    private final SoftwareRepository software;
    private final SoftwareCompilationSets compilationSets;

    @Transactional
    public SoftwareId handle(AddCompilationItemsCommand command) {
        Software compilation = software.findById(command.compilation()).orElseThrow(SoftwareNotFoundException::new);
        compilation.requireChangeableBy(command.actor());
        Set<SoftwareId> contents =
                new HashSet<>(compilation.getCompilationItems().ids());
        if (contents.addAll(command.programs())) {
            compilation.changeCompilationItems(compilationSets.of(compilation.getId(), contents));
            software.save(compilation);
        }
        return compilation.getId();
    }
}
