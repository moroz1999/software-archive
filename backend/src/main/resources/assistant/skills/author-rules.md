---
name: author-rules
description: How people, groups and labels are written down in the archive. Load it before adding, changing or crediting a person, a group or a label.
---

## Names
- A name is written as the source writes it, as a label: works are credited under labels.
- A label in Latin letters is never translated or transliterated: «Ivanov G.» stays as it is, a label of the person whose Russian name is «Георгий Иванов».
- A person's real name is written first name, patronymic, last name, whatever order the source gives: «Иванов Георгий Иванович» on the page is the real name «Георгий Иванович Иванов»; the label stays «Иванов Георгий Иванович».
- A person needs at least one label or a real name. They may choose one label as their display name. With no label chosen, the archive shows the first label by name if there is one, or the real name.
- Set the real name, notes and locations only when the source says them. What the source tells of them that the archive has no place for yet (photos, dates of life, other activities, contacts, links to their pages): tell the editor in your answer, with where it is.
- A name of a group (a team, a studio, a company): it is a group or a label of a group, not a person.

## Credits
- Credit people, groups and labels on the software by default, including names listed on a release page. Credit them on a specific release only for changes the source explicitly attributes to them in that release, such as a crack, intro, adaptation or translation.
- Credit each person, group or label once per entry with all their roles for that entry, using roles from `list_options`. Use the exact label the source gives when it credits a label.
- Credit the creators of the source work on a port, conversion or remake as `original_author` on the derived software.

## A full name and its short forms
Labels of one person written fully and shortly, e.g. «Иванов Георгий Иванович», «Иванов Г.И.» and «Ivanov G.»:
- The person is made of the label with the full name, their real name written as above.
- Every short form stays a label of its own, attached to that person, never merged into them: software was made under it, so it has the right to exist apart.
- No label gives the whole first and last name (only initials, «Иванов Г.И.»): it stays a label, no person is made of it.
- That the labels are one person is still told by the source or by their works (`find-author`), not by the likeness of the names; say in the plan why.
- How to do it with labels already in the archive: `load_skill` with `merge-labels`.

## Combined labels

- Split a label containing `/` into separate labels, trimming the spaces around each name. In «Ivan S./SomeSoft», «Ivan S.» is clearly a person's label. «SomeSoft» could be Ivan's alias, a company or a co-author. Search the archive for both names and compare their credits and who they worked with before merging or attaching either label.
- In «GoodGuy^Some7hing», «GoodGuy» is almost certainly a person's label and «Some7hing» a group's label. Search for both names and check the group's members and the works they are credited on before proposing an attachment or membership.

### Example of sorting out duplicates

The archive has «Иванов Б. / Политех», «Иванов Б. / Гошасофт», «Иванов Борис», «Иванов Б.Ю.» and «Политех». Search for the full names and each part separately. Once the credits or sources confirm the matches, propose the changes in this order.

1. If the person is missing, make them from the fullest label, «Иванов Борис», and set their real name to «Борис Иванов».
2. Split «Иванов Б. / Политех» into «Иванов Б.» and «Политех», and «Иванов Б. / Гошасофт» into «Иванов Б.» and «Гошасофт». Preserve the credits, memberships, locations and source information the combined entries carry. If the evidence or available actions cannot account for that information, tell the editor what needs their decision and leave the affected entry in place.
3. Merge the duplicate «Иванов Б.» labels and reuse the existing «Политех» for the confirmed match. Keep one «Иванов Б.» and one «Иванов Б.Ю.» as separate labels of «Борис Иванов» if both refer to him. Establish what «Политех» and «Гошасофт» refer to from the archive before attaching them or assigning group membership.

## Places
- Find every country and city with `find_places`, by its name in any language.
- A city is always given with its country, even when the source names only the city: you know which country it is in. A city of no known country in the archive whose country you know: put it there (`put_city_in_country`) in the plan.
- A place the archive lacks: add it in the plan before the change that uses it (`create_country`, then `create_city` in it), with its Russian name and, when you know it, the English one.
- A place found twice (two cities of one name in one country, a country kept as a city): see `load_skill` with `merge-places`.

## Groups
- Group membership belongs to the group: it is changed as a change of the group (`set_group_member`, `remove_group_member`). A person who was in a group under a label is a member as that label.
- A group can be a member of another group; that is apart from the labels and people that are members.
