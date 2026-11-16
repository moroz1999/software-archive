# Authors

The archive separates who someone is from the names they use.

## Person
A real person. The real name is optional; a person may be known only through labels. Can have notes.

## Group
A team or a company that releases works. Has a name, notes and at least one group type. Group types are a reference book kept in the archive: demogroup, crack group, game studio, label.

## Label
A name works are released under ("VASYANSOFT"). Works are credited to labels, or to people and groups themselves when the name does not matter — see [software.md](software.md#credits).
A label is taken as it is first met: often nobody knows yet whose it is, so a label can belong to nobody. Once it is found out, the label is attached to its owner — a person (a nickname) or a group (an alternative name of the group) — never to both.
An owner can have several labels. Of a person's labels exactly one is the display name, as a person is known by a nickname rather than by the real name. A group has no display name: its name already is the name it is known by, and its labels are only its other names. Labels can be tied to cities.
A label taken away from its owner is not deleted: it becomes a label of nobody and keeps its credits and memberships. A label is deleted only on purpose.
A label is only a name as some program or source gave it; it means somebody only once a person who knows says whose it is. Equal names are not the same label: a label of nobody is never matched to an owner by its name.
A label of nobody is attached to its owner by hand, from its own page: the person attaching it chooses the owner and, for a person, says whether it becomes their display name. It goes over with its credits and memberships. A person without labels gets it only as the display name, by the rule of exactly one; a group simply gets one more name. Only administrators attach them ([users.md](users.md#user-groups)).
A label that plainly is a person or a group — as is common right after an import — is made into one from its own page: a new person known only by it, their display name; or a new group under its name, of the types chosen, with the label as one of its names. The label goes over with its credits and memberships; the new owner is added by whoever made it, who fills in the rest afterwards. A label that belonged to somebody is taken away from them, but never so that a person is left without their display name while other labels stay, or without any name at all: those are refused.

## Locations
People and groups are located in countries, or in cities of them; one place counts once. An owner with locations has exactly one primary location, the one they are best known from.

## Group membership
Who was in a group is recorded as a person or as the exact alias they used there: the group ZHOPAGROUP had "хакер федя" as a member, an alias of Fyodor Ivanko, who is known as MASTERSOFT today. The member stays "хакер федя". A membership carries one or several roles and may state its start and end days; without a chosen role it is recorded as unknown.
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

A catalog follows the platform switch ([hardware.md](hardware.md#platform-switch)): with a platform chosen it lists only the owners credited on software of that platform, and the labels of nobody credited on it themselves. A label of nobody has no locations: a place filter leaves it out.

Visitors narrow a catalog:
- by the first letter of the entry's name: a Latin letter A–Z, a Cyrillic letter А–Я, or `#` for names starting with anything else (digits, symbols, other scripts). Case does not matter; Ё counts as Е;
- by country and by city, where the owner is located (any of their locations). Cities to choose from are those of the chosen country.

### Author catalog
People and their labels. A person is an entry under the real name, with the display name next to it ("Андрей Мудаков MudakSoft"), or with nothing for a person without labels. Every label of a person has the real name next to it ("Antizhopa Сергей Пидорков"). A person without a real name has no entry of their own: they are found by their labels, each with the main name next to it, the main name itself with nothing.

### Group catalog
Groups and their labels. A group is an entry under its name; every label of it has the name of the group next to it.

## Author page
The page of a person: the main name, the real name, notes, all their labels, their locations (the primary one first) and the groups they were members of, each under the group's main name with the label they were a member under, unless it is their main name ("ZHOPAGROUP as хакер федя"); and the software they are credited on, by themselves or under any of their labels, oldest first, with their roles on each and the label they were credited under, unless it is their main name.

## Label page
Every label has a page of its own: its name, whose it is — a person by the real name with the main name next to it, or by the main name without a real name; a group by its name; leading to their page; or that it is not known yet — the groups it was a member of and the software credited under it; and, for administrators, attaching a label of nobody to its owner; and making a new person or group of the label. The labels on the pages of people and groups lead to their own pages.

## Group page
The page of a group: its name, the group types, notes, all its labels, its locations (the primary one first), its members under the selected person name or alias with their roles and dates — an alias leading to its own page, the person's main name next to it leading to the person — the groups that are members of it and the groups it is a member of; and the software it is credited on, by itself or under any of its labels, as on the author page.

## Adding, editing and deleting
People and groups are added, edited and deleted in the archive; for now anyone can do it.
Deleting a person or a group leaves their labels as labels of nobody, with their credits and memberships; the memberships of a deleted group go with it.
