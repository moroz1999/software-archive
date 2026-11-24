package com.archive.backend.assistant.web.entry;

import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.software.read.CreditedRelease;
import com.archive.backend.software.read.CreditedSoftware;
import java.util.List;

/** A group as its page shows it: the group and what it is credited on, under any of its names. */
public record GroupEntry(GroupDetails group, List<CreditedSoftware> software, List<CreditedRelease> releases) {}
