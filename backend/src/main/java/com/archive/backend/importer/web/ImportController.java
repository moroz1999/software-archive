package com.archive.backend.importer.web;

import com.archive.backend.importer.app.GetLastImportRunHandler;
import com.archive.backend.importer.app.GetLastImportRunQuery;
import com.archive.backend.importer.app.ListBasisCategoriesHandler;
import com.archive.backend.importer.app.ListBasisCategoriesQuery;
import com.archive.backend.importer.app.ListVektorSectionsHandler;
import com.archive.backend.importer.app.ListVektorSectionsQuery;
import com.archive.backend.importer.app.StartImportCommand;
import com.archive.backend.importer.app.StartImportHandler;
import com.archive.backend.importer.domain.basis.BasisCategory;
import com.archive.backend.importer.domain.run.ImportLogSeq;
import com.archive.backend.importer.domain.run.ImportSource;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.importer.web.dto.LastImportRunDto;
import com.archive.backend.importer.web.mapper.ImportWebMapper;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
public class ImportController {

    private final StartImportHandler startImport;
    private final ListBasisCategoriesHandler listBasisCategories;
    private final ListVektorSectionsHandler listVektorSections;
    private final GetLastImportRunHandler getLastImportRun;
    private final ImportWebMapper mapper;

    /**
     * The report of the import started last, whether it goes on or has ended, with the lines of its log after the one
     * given; all of them without one.
     */
    @GetMapping("/last-run")
    public LastImportRunDto lastRun(Actor actor, @RequestParam(required = false) @Nullable Long after) {
        return new LastImportRunDto(getLastImportRun
                .handle(new GetLastImportRunQuery(actor, after == null ? null : new ImportLogSeq(after)))
                .map(mapper::toDto)
                .orElse(null));
    }

    /** The codes of the sections of vektor-06c.ru an import can be started for, in alphabetical order. */
    @GetMapping("/vektor/sections")
    public List<String> vektorSections(Actor actor) {
        return listVektorSections.handle(new ListVektorSectionsQuery(actor));
    }

    /** The categories of "Базис" an import can be started for, in the order of the source. */
    @GetMapping("/basis/categories")
    public List<String> basisCategories(Actor actor) {
        return listBasisCategories.handle(new ListBasisCategoriesQuery(actor));
    }

    /** Starts the import of vektor-06c.ru, or of one section of it by the code in its address; it runs on alone. */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/vektor")
    public ResponseEntity<Void> startVektor(Actor actor, @RequestParam(required = false) @Nullable String section) {
        startImport.handle(new StartImportCommand(
                actor, ImportSource.VEKTOR, section == null ? null : new VektorSection(section)));
        return ResponseEntity.accepted().build();
    }

    /** Starts the import of the whole card index "Базис", or of one category; it runs on alone. */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/basis")
    public ResponseEntity<Void> startBasis(Actor actor, @RequestParam(required = false) @Nullable String category) {
        startImport.handle(new StartImportCommand(
                actor, ImportSource.BASIS, category == null ? null : new BasisCategory(category)));
        return ResponseEntity.accepted().build();
    }
}
