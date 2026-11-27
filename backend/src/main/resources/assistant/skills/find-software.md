---
name: find-software
description: Finding whether software from a source or a request is already in the archive. Load it before adding or changing software from a source.
---

- A same title is only a candidate. Short, generic titles (Demo, Intro, Music, Tetris, Lines) say almost nothing.
- Find candidates both ways:
  - `search_software` with `keyword`, and `search` with kind `software`, by the title and by every other title the source gives;
  - by the source's people and groups: the software and releases in `get_author`, `get_group`, `get_label`, and `search_releases` with `creditedIds`. The work may be stored under another title.
- A file the source offers may already be kept: `search_releases` by `fileName` and, when the digest is known, by `md5`.
- Compare each candidate (`get_software`) with what the source gives. What the source leaves out (no year, no authors) is no evidence either way and no reason to ask.
- People: the source's people and groups against the candidate's credits, each found with `find-author`; a label counts as its owner.
- Also compare the platform, the year (a re-release may be later), the hardware and the category.
- Same work: some of the source's people are the candidate's, and nothing contradicts.
- Another work: none of the source's people is credited on the candidate, or the platform or the year clearly disagree. Add it as new; in the plan name the namesake you checked and why it is another work.
- With no plausible candidate, add the work as new even when its credits or date are unknown. When a plausible candidate remains and its credits cannot be compared with the source, ask the editor to resolve the match before adding or changing that work.
- Never fit stored software to the source. Different people, platform or year mean another work or a wrong record; the editor decides which.
- Same work: add only what the source adds. A stored value the source contradicts stays; mention it in your answer.
- A version of the work (another release type, a crack, an adaptation, a translation) is a release of the stored software, not new software.
