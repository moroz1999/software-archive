# AI assistant

An assistant that helps trusted editors fill and clean the archive. An editor gives it a task in a chat — about the page they are looking at, about a page of another site, or about the archive as a whole — and the assistant researches the archive, asks when something is ambiguous, and proposes a **plan**: an ordered list of changes. Nothing is changed until the editor approves the whole plan.

## Who may use it
Only members of the user group **AI assistant**, given on purpose by an administrator, like the moderator group ([users.md](users.md#user-groups)). It is given only to administrators: the head administrator sees to it; the archive itself does not tie the two. Everyone else sees nothing of the assistant. The assistant acts as the editor who asked it: it sees what they may see, and every change of a plan is made as theirs, by the rules they obey, so a change they may not make fails when the plan runs.

## What it knows of the page
Every message carries the page the editor wrote it from. On the page of an entry the assistant knows which entry it is. On a list — the catalog of software, of authors, of groups, the search results, the fresh software — it knows the list as the editor sees it, with its filters and page, and can read it itself: its pages hold as many entries as the editor's, so the page they are on is the page it reads. So an editor can ask it to find and sort out the duplicates on the page in front of them.

## Reading
The assistant reads freely, without asking: it searches software, releases, people, groups and labels, reads their pages, the catalogs and lists of the archive, its reference books (categories, platforms and hardware, countries and cities, group types, roles, release types, preservation statuses), and pages and pictures of other sites given to it. It looks at the screenshots of the archive too.

### Other sites
The assistant reads only the public internet: an address of the archive's own machine or network, of a private or link-local network, or one that leads there by its name or by a redirect, is refused, and so is any address that is not http or https. A page is read whatever its status and up to 200 KB; a picture is a PNG, JPEG, GIF or WebP of at most 5 MB; a file is downloaded within the limit of what it is for (10 MB for a screenshot, 100 MB for an archive). Nothing of the editor — no cookie, no account — goes with a request. It finds a country or a city by any of its names in any language, letter case, "ё" and "е", spaces and hyphens aside, those of that very name first; a city with its country, if known.

### Release search
Releases are found by the assistant on their own, apart from their software: by a word of their title, their version or the title of their software, by their software, type, release year (a release of unknown date has none), platform, hardware (the release's own, and its software's in the groups the release does not state), who is credited on them (a person or a group is found by themselves and by any of their labels, a label only where it is credited itself) and in which roles, by the name of their archive or of any file inside it, and by the MD5 digest of a file inside. The latter finds a file the archive already keeps under another release.

### What the archive has no place for
What a source tells that would be of use to the archive but that the archive has no place for yet — photos of media, manuals, prices, magazine reviews, cheat codes, a person's photo or dates of life, and the like — the assistant never drops silently and never puts into a field it does not belong to: it tells the editor in its answer what it is and where it is.

### Taking software from a site
- The description of a site is judged first: one that reads as the official description of the work (of its authors, publisher or the box) is taken exactly and in full; informal text of fans is retold as a calm formal description of the facts it clearly states, with nothing added. The assistant tells the editor which way it went and why.
- The rights to what the archive takes from such sites are agreed with their owners, who gave their permission: texts, pictures and files of their pages are taken without any copyright caveats.
- Maps, covers and adverts of the work go into the galleries of the software ([software.md](software.md#pictures)); how it is loaded, run and played goes into its instructions, never into its description.
- A disk, tape or ROM image containing more than one program is imported as a compilation. The assistant plans to add a separate entry for every identified program the archive lacks and reuse confirmed matches. It assigns known release years, credits and screenshots to the programs they describe, then links the programs to the compilation's own entry in the same plan. It tells the editor what it could not identify.
- When importing software, the assistant records the work's base hardware on the software. A release specifies only the hardware groups that differ for that version and inherits the rest ([hardware.md](hardware.md#hardware-of-software-and-releases)).

### Homepages
When the assistant knows the homepage of a program or a compilation it adds or edits, it saves the address in the entry's Links section. Programs with a shared homepage can each link to it.

### Ids
A model copying a long id now and then swaps or mixes its digits, and then finds nothing. So the assistant copies ids exactly, and an id not found is checked against the result it came from and looked up again, never taken for an entry gone.

## Plans
- A plan is one logical act: adding software from a source with its releases and credits, merging a duplicate, and so on. It is approved or dropped as a whole, with one button behind a confirmation that counts its changes and those of them that delete or merge data away; there are no per-change toggles and no undo.
- The assistant assigns credits for the work as a whole to the software. A release gets only contributions that the source explicitly attributes to that version. When someone contributed to the work as a whole and also to a specific version, their credits describe the respective roles on each entry.
- Each change of a plan shows its kind (adding, editing, deleting, crediting or taking a member in, taking a credit or a member away, attaching a label, taking a label away from its owner, merging, making a person or a group of a label, adding files, deleting a file), the entry it changes, and each field it sets beside what the entry holds now. Fields are named in words; entries of the archive (categories and hardware too) are links; the values of the reference books (release types, roles, preservation statuses) are their names; an item of a list (a label, a place, a credit) is a line of its values, with what is marked on it (the name a person is shown by, the main place) in words. An id a change takes from an earlier one is shown as made by that change.
- A change may use what an earlier change of the same plan adds: a release of software added in step 1.
- The changes are made one by one. The first that fails stops the plan; the editor sees what was done and what was not, and the assistant proposes a plan for the rest.
- Writing another message instead of approving drops the plan proposed.
- The editor can stop the assistant at any moment, also in the middle of a plan; the change being made is finished, the rest is not made.

### What a plan can change
- Software: adding, editing, deleting it with its releases, changing its credits, and adding programs to a compilation. A plan can create the programs it adds.
- Releases: adding one (without its archive), editing, deleting; crediting on it and taking a credit away.
- People and groups: adding them with their labels, locations and (of a group) members; editing, deleting (their labels stay, as labels of nobody); taking a member into a group, giving a member other roles, taking one out.
- Labels: adding a label of nobody; renaming and relocating one (whose it is stays); attaching a label of nobody to its person or group, taking a label away from its owner, making a label a label of a group; merging a label into the person, the group or the label it duplicates; making a new person or group of a label of nobody; deleting one.
- Files: adding screenshots of software or of a release, or maps, covers and adverts of software, from pictures of another site, all of them or none; giving a release its archive from a file of another site, under the name the plan gives or the one the site gives; deleting a picture of any gallery. They obey the rules of uploads ([software.md](software.md#screenshots)).
- An edit changes only what it names: the rest of the entry stays as it is when the plan runs. A list it names (credits, labels, members, locations, hardware, categories) is replaced whole.
- Countries and cities: adding them; merging a country or a city into the one it duplicates; putting a city in a country; making a country of a city of no known country ([geo.md](geo.md#keeping)).
- Hardware: adding an item the source clearly names and the reference book lacks ([hardware.md](hardware.md)).
- A category the work clearly belongs to but the archive lacks is not added and not replaced by a near one: the assistant asks the editor.
- Two people, two groups or two software cannot be merged: the assistant reports such duplicates with its evidence instead.

### A full name and its short forms
Labels of one person written fully and shortly («Иванов Георгий Иванович», «Иванов Г.И.», «Ivanov G.»): the person is made of the label with the full name, their real name written first name, patronymic, last name («Георгий Иванович Иванов»); every short form stays a label of its own, attached to the person and never merged into them, since works were made under it. A label in Latin letters is never translated: «Ivanov G.» stays as it is, a label of «Георгий Иванов». A label without the whole first and last name stays a label; no person is made of it. That the labels are one person is still told by a source or by their works, not by the likeness of the names.

## Skills
The assistant's knowledge of how a kind of task is done in this archive — finding which person a name means, finding whether software is already in the archive, adding software from a page of another site — is written down as its skills, which it reads before such a task.

## Language
The assistant answers in the language the editor wrote in. It does not fill names kept in several languages, except of a country, a city or an item of hardware it adds: the Russian name, and the English one when it knows it.

## Conversations
An editor has any number of conversations, each with its own history, and sees only their own; a conversation is titled after its first message. One task runs in a conversation at a time: the editor writes again once the assistant has answered, proposed a plan or been stopped. A task takes at most 50 steps and 5 minutes; past them the assistant stops and says so. A task the assistant was in the middle of when the archive restarted fails, and a change of a plan being made then is not made again. A conversation stays until the editor deletes it.
