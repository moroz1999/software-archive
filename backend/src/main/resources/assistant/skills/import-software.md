---
name: import-software
description: Adding software from a page of another site.
---

- Read the page with the tools for other sites. Take only what the page says.
- First identify the page's people and groups: call `load_skill` with `find-author` and follow it.
- Then call `load_skill` with `find-software` and follow it. Same work: offer to change the stored software, not to add another.
- Add a person, a group or a label only when no one of that name exists, or when `find-author` says the stored namesake is somebody else; say so in the plan.
- Software is for exactly one platform: find it with `list_platforms`.
- Put it into a leaf category of `list_categories` (e.g. Arcade under Games), never into a top-level section.
- Credits: each person, group or label once, with all their roles from `list_options` (developer, publisher, code, music…). Credit the label the page names, not its owner instead. Work on a copy (cracking, intro, adaptation, translation) is a credit of the release, not of the software.
- Hardware is split between the software and its releases. The software holds its base hardware: what every release needs. A release states only what is its own (another machine of the platform, a floppy instead of a tape, an AY): a group it states replaces that group of the software.
- Copy the page's description of the work into the software's description: exactly and in full, no rewording, no retelling, no cuts, nothing of your own. Do not invent a description.
- A release gets a description only when the page describes that version apart from the work, e.g. the changes of each version in a list of several.
- Always add a release, even without a downloadable file; give it its type, release date, version and credits.
- Every downloadable file of the work (another version, another language, another medium) is a release of its own, with that file as its archive.
- The release date is a year, or a day when the page gives the day. Never turn a year into a made-up day.
- Add the page's screenshots of the work to the software, those of one version to that release. Look at a picture when unsure what it shows.
