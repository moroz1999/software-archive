# Authors

The archive separates who someone is from the names they use.

## Person
A real person. The real name is optional; a person may be known only through labels. Can have notes.

## Group
A team or a company that releases works. Has a name, notes and group types, if known. Group types are a reference book kept in the archive: demogroup, crack group, game studio, label.

## Label
A name works are released under ("VASYANSOFT"). Works are credited to labels, or to people and groups themselves when the name does not matter — see [software.md](software.md#credits).
A label is taken as it is first met: often nobody knows yet whose it is, so a label can belong to nobody. Once it is found out, the label is attached to its owner — a person (a nickname) or a group (an alternative name of the group) — never to both.
An owner can have several labels. Of a person's labels exactly one is the display name, as a person is known by a nickname rather than by the real name. A group has no display name: its name already is the name it is known by, and its labels are only its other names.
A label taken away from its owner is not deleted: it becomes a label of nobody and keeps its credits and memberships. A label is deleted only on purpose.
A label is only a name as some program or source gave it; it means somebody only once a person who knows says whose it is. Equal names are not the same label: a label of nobody is never matched to an owner by its name.
A label of nobody is attached to its owner by hand, from its own page: the person attaching it chooses the owner and, for a person, says whether it becomes their display name. It goes over with its credits and memberships; where software or a release credits both the owner and the label, the owner's credit joins the label's, with the roles of both ([software.md](software.md#credits)). A person without labels gets it only as the display name, by the rule of exactly one; a group simply gets one more name. Only administrators attach them ([users.md](users.md#user-groups)).
A label is added by whoever gave it first: the user who typed it in the form of a person or a group, or on its own as a label of nobody — a name met in a program before anybody knows whose it is — or an import, whose labels are added by the importer ([imports.md](imports.md)). A label added on its own gets its name and locations; whose it is, is said later from its page. A name equal to another label's makes a label of its own.
A label that plainly is a person or a group — as is common right after an import — is made into one from its own page. Making a person replaces the label with a new person of the same name: the label ceases to exist, while its credits, group memberships, locations and remembered origin become direct relations of the person. Making a group replaces the label the same way with a new group of its name, of the types chosen: its credits, locations and remembered origin become the group's, and the groups it was a member of have the group as a member group — with no roles, as a group member has none. Whoever added the label, and administrators, make a new owner of it; the new owner is added by whoever made it, who fills in the rest afterwards. A label that belonged to somebody is taken away from them, but never so that a person is left without their display name while other labels stay, or without any name at all: those are refused.
A label found out to be a duplicate of a group — the same group under another name, as imports often bring — is merged into that group from its own page, so that nothing it brought is lost and imports keep finding it. Only a label of nobody or of a group is merged, never a label of a person. The label ceases to exist, and what it had becomes the group's directly: its credits, joined with a credit the group already has on the same software into one with the roles of both (an unknown role giving way to a known one); its memberships, the groups it was a member of having the group as a member group, with no roles, once even when it already was one, and none in the group itself; its locations, added to the group's, each place once, the group's primary staying primary; and its remembered origin. A label found out to be a duplicate of a person, or of another label, is merged into them the same way, by the same rules: never a label of a person, its credits and memberships joined with those the person or the other label already has, one each with the roles of both; its locations added to theirs, each place once, their primary staying primary; and its remembered origin, so that imports find the person or the other label. A label is merged into any other label — of nobody, of a person or of a group — but never into itself, and the other label stays whose it was. A label found out to be only another name of a group is made a label of the group from its own page instead: a label of nobody is attached to it, a label of another group moves to it, and either keeps its credits and memberships under its name; a label already of the group stays as it is. Again, never a label of a person. A label that adds nothing its owner lacks is merged into them, not attached. Only administrators merge labels and make them labels of a group ([users.md](users.md#user-groups)).

### Editing a label
A label is edited on its own page: its name, its locations and whose it is. Renaming keeps it the same label, with its credits and memberships. Where an import took the label from — the source archive, the id there and the link to it, if the source has one — is shown and never edited.
Only whoever added the label, and administrators, edit and delete it ([users.md](users.md#user-groups)). Only administrators say whose it is and change that: they attach it to a person, saying whether it becomes their display name, or to a group, or leave it to nobody — by the rules of attaching a label and taking it away. Which label is the display name of a person who already has the label is chosen in the person's own form.
Deleting a label removes it together with its credits, its memberships and its locations; an import forgets it, so a later import meeting the name takes it as a new label. A label a person cannot lose — their display name while other labels stay, or their only name — is not deleted.

## Locations
People, groups and labels are located in countries, or in cities of them; one place counts once. Whoever has locations has exactly one primary location, the one they are best known from. A label is located on its own, even of nobody: a program often tells where its makers come from long before anybody knows whether they are a person or a group.

## Group membership
Who was in a group is recorded as a person or as the exact alias they used there: the group ZHOPAGROUP had "хакер федя" as a member, an alias of Fyodor Ivanko, who is known as MASTERSOFT today. The member stays "хакер федя". A membership carries one or several roles, and nothing else; without a chosen role it is recorded as unknown.
Membership belongs to the group: it is always changed as a change of the group, even when it is started from the page of a person.
A group can also be a member of another group. This is a relation of its own, between the groups themselves, apart from the labels that are members.
The labels of a group are its alternative names (ZHOPAS, ZS), not its members.

## Main name
A person is shown under one main name: their display name; for a record from before the rule with no label marked, the first of their labels in alphabetical order; without labels, the real name. A person without any name is not an author of the archive.
A group is always shown under its name.

## Credits under a label
Wherever a work names who made it — its card, its credits, the works of a person or a group — a credit under a label shows that label, never the owner's main name in its place. A label shown anywhere leads to its own page, which tells whose it is.

## Catalogs of names
The author catalog and the group catalog are where visitors find authors and groups. Each lists its owners and their labels mixed together, sorted by name, 100 entries per page:
- an owner is an entry leading to the owner;
- every label of theirs, the display name too, is an entry of its own leading to the label's page;
- a label of nobody is an entry of the author catalog with nothing next to its name, leading to its own page: it may as well be a group's, but nobody knows yet. It is not in the group catalog.

A catalog follows the platform switch ([hardware.md](hardware.md#platform-switch)): with a platform chosen it lists only the owners credited on software of that platform, and the labels of nobody credited on it themselves. A place filter goes by where the owners are located: a label of nobody is left out of it.

Visitors narrow a catalog:
- by the first letter of the entry's name: a Latin letter A–Z, a Cyrillic letter А–Я, or `#` for names starting with anything else (digits, symbols, other scripts). Case does not matter; Ё counts as Е;
- by country and by city, where the owner is located (any of their locations). Cities to choose from are those of the chosen country.

### Author catalog
People and their labels. A person is an entry under the real name, with the display name next to it ("Андрей Мудаков MudakSoft"), or with nothing for a person without labels. Every label of a person has the real name next to it ("Antizhopa Сергей Пидорков"). A person without a real name has no entry of their own: they are found by their labels, each with the main name next to it, the main name itself with nothing.

### Group catalog
Groups and their labels. A group is an entry under its name; every label of it has the name of the group next to it.

## Author page
The page of a person: the main name, the real name, notes, all their labels, their locations (the primary one first) and the groups they were members of, each under the group's main name with the label they were a member under, unless it is their main name ("ZHOPAGROUP as хакер федя"); and the software they are credited on, by themselves or under any of their labels, oldest first, with their roles on each and the label they were credited under, unless it is their main name; then, the same way, the releases they are credited on, each with its software, type, date and version ([software.md](software.md#releases)).

## Label page
Every label has a page of its own: its name, whose it is — a person by the real name with the main name next to it, or by the main name without a real name; a group by its name; leading to their page; or that it is not known yet — its locations (the primary one first), the groups it was a member of, and the software and the releases credited under it; and, for administrators, attaching a label of nobody to its owner, merging a label into a group, a person or another label, and making it a label of a group; for whoever may, editing the label and making a new person or group of it. The labels on the pages of people and groups lead to their own pages.

## Group page
The page of a group: its name, the group types, notes, all its labels, its locations (the primary one first), its members under the selected person name or alias with their roles — an alias leading to its own page, the person's main name next to it leading to the person — the groups that are members of it and the groups it is a member of; and the software and the releases it is credited on, by itself or under any of its labels, as on the author page.

## Adding, editing and deleting
People and groups are added, edited and deleted in the archive; for now anyone can do it.
Deleting a person or a group leaves their labels as labels of nobody, with their credits and memberships; the memberships of a deleted group go with it.
