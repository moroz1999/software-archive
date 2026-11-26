---
name: author-rules
description: How people, groups and labels are written down in the archive. Load it before adding, changing or crediting a person, a group or a label.
---

## Names
- A name is written as the source writes it, as a label: works are credited under labels.
- A label in Latin letters is never translated or transliterated: «Ivanov G.» stays as it is, a label of the person whose Russian name is «Георгий Иванов».
- A person's real name is written first name, patronymic, last name, whatever order the source gives: «Иванов Георгий Иванович» on the page is the real name «Георгий Иванович Иванов»; the label stays «Иванов Георгий Иванович».
- A person's display name is the name they are known by; a person needs at least one label or a real name.
- Set the real name, notes and locations only when the source says them. What the source tells of them that the archive has no place for yet (photos, dates of life, other activities, contacts, links to their pages): tell the editor in your answer, with where it is.
- A name of a group (a team, a studio, a company): it is a group or a label of a group, not a person.

## A full name and its short forms
Labels of one person written fully and shortly, e.g. «Иванов Георгий Иванович», «Иванов Г.И.» and «Ivanov G.»:
- The person is made of the label with the full name, their real name written as above.
- Every short form stays a label of its own, attached to that person, never merged into them: software was made under it, so it has the right to exist apart.
- No label gives the whole first and last name (only initials, «Иванов Г.И.»): it stays a label, no person is made of it.
- That the labels are one person is still told by the source or by their works (`find-author`), not by the likeness of the names; say in the plan why.
- How to do it with labels already in the archive: `load_skill` with `merge-labels`.

## Places
- Find every country and city with `find_places`, by its name in any language.
- A city is always given with its country, even when the source names only the city: you know which country it is in. A city of no known country in the archive whose country you know: put it there (`put_city_in_country`) in the plan.
- A place the archive lacks: add it in the plan before the change that uses it (`create_country`, then `create_city` in it), with its Russian name and, when you know it, the English one.
- A place found twice (two cities of one name in one country, a country kept as a city): see `load_skill` with `merge-places`.

## Groups
- Group membership belongs to the group: it is changed as a change of the group (`set_group_member`, `remove_group_member`). A person who was in a group under a label is a member as that label.
- A group can be a member of another group; that is apart from the labels and people that are members.
