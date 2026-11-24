---
name: add-authors
description: Adding people, groups and labels, e.g. from a scene page, a group roster or a credits list.
---

- First call `load_skill` with `find-author` and find every name as it says.
- A person already in the archive: change that person. Do not add another one.
- Another name of a known person or group: a label of them, not a new person or group.
- A name whose owner the source does not tell: a label of nobody, as it is met. Never guess its owner.
- A person's display name is the name they are known by; a person needs at least one label or a real name.
- Set the real name, notes and locations only when the source says them. Countries and cities: find them with `list_countries` and `list_cities`; never invent one.
- Group membership belongs to the group: it is changed as a change of the group. A person who was in a group under a label is a member as that label.
- A group can be a member of another group; that is apart from the labels and people that are members.
- One plan: new people and groups first, then their labels, then credits and memberships.
