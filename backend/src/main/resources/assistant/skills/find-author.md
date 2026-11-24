---
name: find-author
description: Finding which person, group or label of the archive a name from a source or a request means. Load it before crediting, adding or changing anybody.
---

- A name is first of all a label: works are credited under labels, and a label may belong to a person, to a group or to nobody yet.
- Search the name with `find_names`: it finds people, groups and every label, labels of nobody too. Search also with `search` (kinds `author`, `group`): it finds people by their real name as well.
- Several candidates with this name: choose the most similar one. Check with `get_author`, `get_group`, `get_label`:
  - the same real name, country or city;
  - a member of the group the source names (the group's members in `get_group`, the person's groups in `get_author`);
  - credited on other works of the same group or with the same people (`search_releases` with `creditedIds`, the software in `get_author` and `get_group`).
- Equal names are not the same label. Never take a label for somebody's because the names match: only a source or the editor says whose a label is.
- Write in the plan why you chose this person, group or label.
- Nothing matches, but there are candidates with this name: ask the editor. Do not guess.
- Nobody with this name: the name is new.
- A name of a group (a team, a studio, a company): it is a group or a label of a group, not a person.
