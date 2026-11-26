---
name: add-authors
description: Adding people, groups and labels, e.g. from a scene page, a group roster or a credits list.
---

- First call `load_skill` with `find-author` and find every name as it says; then `load_skill` with `author-rules` and write everybody by it.
- A person already in the archive: change that person (`update_author`). Do not add another one.
- Another name of a known person or group: a label of them, not a new person or group: in `labels` of `update_author` or `update_group`, or `attach_label` for a label of nobody found to be theirs.
- A name whose owner the source does not tell: a label of nobody (`create_label`), as it is met. Never guess its owner.
- Labels already in the archive that are to be merged or converted: `load_skill` with `merge-labels`.
- One plan: new places first, then new people and groups (`create_author`, `create_group`, with their labels), then the labels of nobody, then credits (`set_software_credit`, `set_release_credit`) and memberships. A later change takes the id an earlier one adds.
