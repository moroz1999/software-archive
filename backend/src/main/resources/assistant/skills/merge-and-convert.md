---
name: merge-and-convert
description: What each merge and conversion of the archive does — of labels, countries and cities — and which one a case needs. Load it before merging, converting or handing over a label.
---

Every one of these changes is final: nothing undoes it. Read both entries before, and say in the plan why they are one.

## Labels
A label is a name works are credited under; it belongs to a person, to a group, or to nobody while that is not known.

- `attach_label`: a label of nobody turns out to be a person's or a group's. It becomes theirs with its credits and memberships; a credit of the owner on the same software joins the label's, with the roles of both. Of a person it can become the name they are shown by (`display`); a person without labels gets it only as that name.
- `detach_label`: a label taken for somebody's is not theirs. It stays, a label of nobody, with its credits and memberships. Refused when the person would be left without the name they are shown by while other labels stay, or without any name.
- `give_label_to_group`: a label of nobody, or of another group, is only another name of a group. It becomes the group's, keeping its credits and memberships under its name. Never a label of a person.
- `merge_label_into_group`, `merge_label_into_author`, `merge_label_into_label`: a label is a duplicate of a group, a person or another label — the same one under a misspelled or other written name, as imports often bring. The label is deleted; its credits (joined with theirs, one each with the roles of both), memberships, locations and remembered origin go to the one it is merged into, so imports find them. Only a label of nobody or of a group is merged, never a label of a person, and never into itself; the other label stays whose it was.
- `make_author_of_label`: a label is a person not yet in the archive. A new person of its name replaces the label: its credits, memberships, locations and origin become the person's. Look for the person with `find-author` first. A label of somebody is taken away from them, by the rules of `detach_label`.
- `make_group_of_label`: the same for a group, of the types given; the groups the label was a member of get the new group as a member group.
- Choosing: another name of somebody, written apart (a nickname, an old name), is attached or given, not merged: merging loses the name. Merge only a name that is the same name written otherwise.
- A label that adds nothing the owner lacks (only their label or real name, e.g. «Roger M.W. /Alcosoft» for Alcosoft, real name Roger M.W.) is merged into them, not attached.

### A full name and its short forms
Labels of one person written fully and shortly, e.g. «Иванов Георгий Иванович», «Иванов Г.И.» and «Ivanov G.»:
- The person is made of the label with the full name (`make_author_of_label`), and their real name is written first name, patronymic, last name: «Георгий Иванович Иванов» (`update_author`, `realName`).
- Every short form stays a label of its own and is attached to that person (`attach_label`), never merged into them: software was made under it, so it has the right to exist apart.
- A label in Latin letters is never translated or transliterated: «Ivanov G.» stays as it is, a label of the person whose Russian name is «Георгий Иванов».
- No label gives the whole first and last name (only initials, «Иванов Г.И.»): it stays a label, no person is made of it.
- That the labels are one person is still told by the source or by their works (`find-author`), not by the likeness of the names; say in the plan why.

## Places
- `merge_city`: two cities of one country, one name written otherwise (or a city of no known country that is a city of a country). Every place in it goes to the other city and its country; its names go with it. Keep the one named correctly.
- `merge_country`: two countries that are one. Its cities and every place in it go to the other one.
- `put_city_in_country`: a city of no known country whose country is known, or a city put in a wrong country. Every place in it follows it.
- `turn_city_into_country`: a city of no known country that is a country itself (an import took a country for a city). It becomes a country with its names, every place in it is in that country. A country of the same name already in the archive is then a duplicate: merge it (`merge_country`).

## What cannot be merged
Two people, two groups or two software cannot be merged. Report such duplicates with your evidence instead of a plan; what one has that the other lacks can still be added to the other (`update_author`, `update_group`, `update_software`) if the editor asks.
