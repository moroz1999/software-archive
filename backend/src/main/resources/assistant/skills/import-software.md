---
name: import-software
description: Adding software from a page of another site.
---

- What the page tells that the archive has no place for yet (box and cover pictures, manuals, prices, publishers' adverts, magazine reviews, loading instructions, cheat codes, a story of the work and so on): never drop it silently and never squeeze it into a field it does not belong to (the description is the page's description of the work, nothing else). Tell the editor in your answer what it is and where it is.
- Read the page with `fetch_url`; a page cut at its limit (`truncated`) may hold more than you see. Take only what the page says.
- First identify the page's people and groups: call `load_skill` with `find-author` and follow it.
- Then call `load_skill` with `find-software` and follow it. Same work: offer to change the stored software (`update_software`), not to add another (`create_software`).
- Add a person, a group or a label only when no one of that name exists, or when `find-author` says the stored namesake is somebody else; say so in the plan.
- Software is for exactly one platform: find it with `list_platforms`.
- Put it into a leaf category of `list_categories` (e.g. Arcade under Games), never into a top-level section. A category the work clearly belongs to but the archive lacks: do not put it into a near one instead; ask the editor in your answer, naming the category and where in the tree it would go.
- Credits: each person, group or label once, with all their roles from `list_options` (developer, publisher, code, music…). Credit the label the page names, not its owner instead. Work on a copy (cracking, intro, adaptation, translation) is a credit of the release, not of the software. Who made the original that a port, a conversion or a remake is made from is credited on it as `original_author`.
- Hardware: the codes of `list_hardware` of the platform. Hardware the source clearly names but the list lacks: add it in the plan before the software (`create_hardware`, in its group, with a code of lower-case Latin letters, digits and hyphens) and say so. Never add hardware the source only hints at.
- Hardware is split between the software and its releases. The software holds its base hardware: what every release needs. A release states only what is its own (another machine of the platform, a floppy instead of a tape, an AY): a group it states replaces that group of the software.
- Copy the page's description of the work into the software's description: exactly and in full, no rewording, no retelling, no cuts, nothing of your own. Do not invent a description.
- A release gets a description only when the page describes that version apart from the work, e.g. the changes of each version in a list of several.
- Always add a release (`create_release`), even without a downloadable file; give it its type, release date, version and credits.
- Every downloadable file of the work (another version, another language, another medium) is a release of its own, with that file as its archive: `set_release_archive` after `create_release`, by the file's own address, not the page's. Before, look for it with `search_releases` by `fileName`: the archive may keep it already.
- The release date is a year, or a day when the page gives the day. Never turn a year into a made-up day.
- Add the page's screenshots of the work to the software, those of one version to that release (`add_screenshots`, by the pictures' own addresses). Look at a picture with `view_image` when unsure what it shows. A screenshot is a screen of the work only: logos and banners of the site are left out; photos of boxes, covers, tapes, disks, manuals, scans and adverts are not screenshots either, but the archive wants them: list them, with their addresses, in your answer for the editor.
