---
name: find-author
description: Finding which person, group or label of the archive a name from a source or a request means. Load it before crediting, adding or changing anybody.
---

- A name is first of all a label: works are credited under labels, and a label may belong to a person, to a group or to nobody yet.
- Search every name the source gives, and every way of writing it:
  - `find_names`: people, groups and every label, labels of nobody too;
  - `search` with kinds `author` and `group`: it finds people by their real name as well.
- A person the source names by first and last name: search the real name with the place the source gives (`get_author` of each candidate shows their locations), then without the place, with the words swapped (Ivan Petrov, Petrov Ivan) and transliterated (Иван Петров, Ivan Petrov). A nickname given beside the name is searched as a label too.
- A name that does not say what it is (a nickname, an abbreviation): search it among the labels and among the groups alike. A group may be credited under its label, a person under a name that looks like a group's.
- Several candidates with this name: choose the most similar one. Check with `get_author`, `get_group`, `get_label`:
  - the same real name, country or city;
  - a member of the group the source names (the group's members in `get_group`, the person's groups in `get_author`);
  - credited on other works of the same group or with the same people (`search_releases` with `creditedIds`, the software in `get_author` and `get_group`).
- Equal names are not the same label. Never take a label for somebody's because the names match: only a source or the editor says whose a label is.
- Write in the plan why you chose this person, group or label.
- Nothing matches, but there are candidates with this name: ask the editor. Do not guess.
- Nobody with this name: the name is new. What it becomes is told by the source:
  - the source says who it is, a person (a real name, a biography) or a group (a team, a studio, a company, a group roster): a new person (`create_author`, the name their label) or a new group (`create_group`);
  - the source does not say whose the name is: a label of nobody (`create_label`). Never make a person or a group of a bare nickname.
- A name of a group (a team, a studio, a company): it is a group or a label of a group, not a person.
