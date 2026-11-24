package com.archive.backend.assistant.web.entry;

import com.archive.backend.label.read.LabelDetails;
import com.archive.backend.software.read.CreditedRelease;
import com.archive.backend.software.read.CreditedSoftware;
import java.util.List;

/** A label as its page shows it: the label and what is credited under it. */
public record LabelEntry(LabelDetails label, List<CreditedSoftware> software, List<CreditedRelease> releases) {}
