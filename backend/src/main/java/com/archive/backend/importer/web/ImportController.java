package com.archive.backend.importer.web;

import com.archive.backend.importer.app.ListVektorSectionsHandler;
import com.archive.backend.importer.app.ListVektorSectionsQuery;
import com.archive.backend.importer.app.StartVektorImportCommand;
import com.archive.backend.importer.app.StartVektorImportHandler;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportController {

    private final StartVektorImportHandler startVektorImport;
    private final ListVektorSectionsHandler listVektorSections;

    /** The codes of the sections of vektor-06c.ru an import can be started for, in alphabetical order. */
    @GetMapping("/vektor/sections")
    public List<String> vektorSections(Actor actor) {
        return listVektorSections.handle(new ListVektorSectionsQuery(actor));
    }

    /** Starts the import of vektor-06c.ru, or of one section of it by the code in its address; it runs on alone. */
    @PostMapping("/vektor")
    public ResponseEntity<Void> startVektor(Actor actor, @RequestParam(required = false) @Nullable String section) {
        startVektorImport.handle(
                new StartVektorImportCommand(actor, section == null ? null : new VektorSection(section)));
        return ResponseEntity.accepted().build();
    }
}
