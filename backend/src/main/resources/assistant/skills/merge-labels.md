---
name: merge-labels
description: What each merge and conversion of labels does, and which one a case needs. Load it before merging, converting or handing over a label.
---

Every one of these changes is final: nothing undoes it. Read both entries before, and say in the plan why they are one. How names are written: `load_skill` with `author-rules`.

A label is a name works are credited under; it belongs to a person, to a group, or to nobody while that is not known.

- `attach_label` gives an unowned label to its person or group with its credits and memberships. A credit of the owner on the same software joins the label's, with the roles of both. For a person, `display` can make the attached label their display name. The former display label stays with the person and loses its mark.
- `detach_label` leaves the label unowned with its credits and memberships. The person must keep a real name or another label.
- `give_label_to_group`: a label of nobody, or of another group, is only another name of a group. It becomes the group's, keeping its credits and memberships under its name. Never a label of a person.
- `merge_label_into_group`, `merge_label_into_author`, `merge_label_into_label`: a label is a duplicate of a group, a person or another label — the same one under a misspelled or other written name, as imports often bring. The label is deleted; its credits (joined with theirs, one each with the roles of both), memberships, locations and remembered origin go to the one it is merged into, so imports find them. Only a label of nobody or of a group is merged, never a label of a person, and never into itself; the other label stays whose it was.
- `make_author_of_label`: a label is a person not yet in the archive. A new person of its name replaces the label: its credits, memberships, locations and origin become the person's. Look for the person with `find-author` first. A label of somebody is taken away from them, by the rules of `detach_label`.
- `make_group_of_label`: the same for a group, of the types given; the groups the label was a member of get the new group as a member group.
- Choosing: another name of somebody, written apart (a nickname, an old name), is attached or given, not merged: merging loses the name. Merge only a name that is the same name written otherwise.
- For a label containing more than one name, follow the combined-label rules in `author-rules` to decide what each part refers to before merging.
- A full name and its short forms of one person: the person is made of the full one (`make_author_of_label`, then their real name with `update_author`, `realName`); every short form is attached to them (`attach_label`), never merged.

## What cannot be merged
Two people, two groups or two software cannot be merged. Report such duplicates with your evidence instead of a plan; what one has that the other lacks can still be added to the other (`update_author`, `update_group`, `update_software`) if the editor asks.
