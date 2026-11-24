---
name: add-authors
description: Adding people, groups and labels, e.g. from a scene page, a group roster or a credits list.
---

- First call `load_skill` with `find-author` and find every name as it says.
- A person already in the archive: change that person (`update_author`). Do not add another one.
- Another name of a known person or group: a label of them, not a new person or group: in `labels` of `update_author` or `update_group`, or `attach_label` for a label of nobody found to be theirs.
- A name whose owner the source does not tell: a label of nobody (`create_label`), as it is met. Never guess its owner.
- A person's display name is the name they are known by; a person needs at least one label or a real name.
- Set the real name, notes and locations only when the source says them.
- Places:
  - find every country and city with `find_places`, by its name in any language;
  - a city is always given with its country, even when the source names only the city: you know which country it is in. A city of no known country in the archive whose country you know: put it there (`put_city_in_country`) in the plan;
  - a place the archive lacks: add it in the plan before the change that uses it (`create_country`, then `create_city` in it), with its Russian name and, when you know it, the English one;
  - a place found twice (two cities of one name in one country, a country kept as a city): see `load_skill` with `merge-and-convert`.
- Group membership belongs to the group: it is changed as a change of the group (`set_group_member`, `remove_group_member`). A person who was in a group under a label is a member as that label.
- A group can be a member of another group; that is apart from the labels and people that are members.
- One plan: new places first, then new people and groups (`create_author`, `create_group`, with their labels), then the labels of nobody, then credits (`set_software_credit`, `set_release_credit`) and memberships. A later change takes the id an earlier one adds.
