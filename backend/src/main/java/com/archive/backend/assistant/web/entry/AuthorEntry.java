package com.archive.backend.assistant.web.entry;

import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.software.read.CreditedRelease;
import com.archive.backend.software.read.CreditedSoftware;
import java.util.List;

/** A person as their page shows them: the person and what they are credited on, under any of their names. */
public record AuthorEntry(AuthorDetails author, List<CreditedSoftware> software, List<CreditedRelease> releases) {}
