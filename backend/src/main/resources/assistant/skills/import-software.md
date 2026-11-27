---
name: import-software
description: Adding software from a page of another site.
---

- All rights to what the archive takes from other sites are agreed with their owners, who gave their permission: take the texts, pictures and files of the page without any copyright caveats or warnings.
- What the page tells that the archive has no place for yet (photos of tapes and disks, manuals, prices, magazine reviews, cheat codes, a story of the work and so on): never drop it silently and never squeeze it into a field it does not belong to (the description is the page's description of the work, nothing else; the instructions are how to load, run and play it, nothing else). Tell the editor in your answer what it is and where it is.
- Read the page with `fetch_url`; a page cut at its limit (`truncated`) may hold more than you see. Take only what the page says.
- First identify the page's people and groups: call `load_skill` with `find-author` and follow it.
- Then call `load_skill` with `find-software` and follow it. Same work: offer to change the stored software (`update_software`), not to add another (`create_software`).
- Add a person, a group or a label only when no one of that name exists, or when `find-author` says the stored namesake is somebody else; say so in the plan. Before adding or crediting anybody, call `load_skill` with `author-rules` and follow it.
- Software is for exactly one platform: find it with `list_platforms`.
- Put it into a leaf category of `list_categories` (e.g. Arcade under Games), never into a top-level section. A category the work clearly belongs to but the archive lacks: do not put it into a near one instead; ask the editor in your answer, naming the category and where in the tree it would go.
- Follow `author-rules`. Put all credits for the work as a whole on the software. Put only contributions that the source explicitly attributes to that version on its release. Keep shared roles on the software, including when the same person also has a version-specific role.
- Hardware: the codes of `list_hardware` of the platform. Hardware the source clearly names but the list lacks: add it in the plan before the software (`create_hardware`, in its group, with a code of lower-case Latin letters, digits and hyphens) and say so. Never add hardware the source only hints at.
- Put the base hardware on the software. On a release, specify only the hardware groups that differ for that version. Give the full set of hardware for each changed group, since it replaces the corresponding group on the software. The release inherits all other groups.
- First judge the page's description of the work. One that reads as official (by its authors or publisher, from the box or the manual, calm and factual) goes into the software's description exactly and in full: no rewording, no retelling, no cuts. Informal text of fans (jokes, slang, exclamations, personal impressions) is retold as a calm formal description of the facts it clearly states, nothing added, nothing guessed. Tell the editor which way you went and why. Do not invent a description.
- How to load, run and play the work (loading commands, keys, controls, passwords to start) goes into `instructions`, never into the description; the same judging holds for it.
- A release gets a description only when the page describes that version apart from the work, e.g. the changes of each version in a list of several.
- Always add a release (`create_release`), even without a downloadable file. Give it its type, release date and version.
- Every downloadable file of the work (another version, another language, another medium) is a release of its own, with that file as its archive: `set_release_archive` after `create_release`, by the file's own address, not the page's. Before, look for it with `search_releases` by `fileName`: the archive may keep it already.
- The release date is a year, or a day when the page gives the day. Never turn a year into a made-up day.
- Add the page's screenshots of the work to the software, those of one version to that release (`add_screenshots`, by the pictures' own addresses). Look at a picture with `view_image` when unsure what it shows. A screenshot is a screen of the work only: logos and banners of the site are left out; photos of boxes, tapes, disks, manuals and scans are not screenshots either.
- Maps of the work go into the gallery `maps`, covers of its box, tape or disk into `covers`, adverts of it into `adverts`: `add_screenshots` with that `gallery`, on the software, never on a release. Other pictures the archive has no place for: list them, with their addresses, in your answer for the editor.
- If the work's homepage is known, pass it as `sourceUrl` of `create_software` or `update_software`. The address appears in the software's Links section. Several works can share a homepage.

## Compilations

Treat a disk, tape or ROM image that contains more than one program as a compilation, even if the source presents the image as a single program.

1. Read the full list of programs before proposing the plan. Follow links to the contents and to pages for individual programs. Use `find-software` for every named program to identify the work and find any existing archive entry.
2. Give each identified program a separate software entry in the same plan. Use `create_software` for missing programs, reuse confirmed matches and fill in missing facts the source supplies. Give each program its own known release year, credits and screenshots. Resolve names with `find-author` and apply `author-rules`. Assign screenshots with `add_screenshots` and use `view_image` when the captions leave the subject unclear. Leave unknown dates and credits unset.
3. Create a separate software entry for the compilation or reuse a confirmed match. Fill in the compilation's homepage and each program's homepage when known. Put screenshots of the compilation's menu on the compilation.
4. Link all the programs to the compilation with `add_compilation_items`. Refer to existing entries by their ids and to entries created in the same plan by `{"$ref": N}`. Tell the editor which programs or screenshots could not be identified and whether any of the contents could not be read.
