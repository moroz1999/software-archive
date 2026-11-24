# AI assistant

An assistant that helps trusted editors fill and clean the archive. An editor gives it a task in a chat — about the page they are looking at, about a page of another site, or about the archive as a whole — and the assistant researches the archive, asks when something is ambiguous, and proposes a **plan**: an ordered list of changes. Nothing is changed until the editor approves the whole plan.

## Who may use it
Only members of the user group **AI assistant**, given on purpose by an administrator, like the moderator group ([users.md](users.md#user-groups)). Everyone else sees nothing of the assistant. The assistant acts as the editor who asked it: it sees what they may see, and every change of a plan is made as theirs, by the rules they obey, so a change they may not make fails when the plan runs.

## What it knows of the page
Every message carries the page the editor wrote it from. On the page of an entry the assistant knows which entry it is. On a list — the catalog of software, of authors, of groups, the search results, the fresh software — it knows the list as the editor sees it, with its filters and page, and can read it itself. So an editor can ask it to find and sort out the duplicates on the page in front of them.

## Reading
The assistant reads freely, without asking: it searches software, releases, people, groups and labels, reads their pages, the catalogs and lists of the archive, its reference books (categories, platforms and hardware, countries and cities, group types, roles, release types, preservation statuses), and pages and pictures of other sites given to it.

### Release search
Releases are found by the assistant on their own, apart from their software: by a word of their title, their version or the title of their software, by their software, type, release year (a release of unknown date has none), platform, hardware (the release's own, and its software's in the groups the release does not state), who is credited on them (a person or a group is found by themselves and by any of their labels, a label only where it is credited itself) and in which roles, by the name of their archive or of any file inside it, and by the MD5 digest of a file inside. The latter finds a file the archive already keeps under another release.

## Plans
- A plan is one logical act: adding software from a source with its releases and credits, merging a duplicate, and so on. It is approved or dropped as a whole, with one button behind a confirmation that counts the destructive changes; there are no per-change toggles and no undo.
- Each change of a plan shows its kind (adding, editing, deleting, merging, making a person or a group of a label, attaching a label, adding files, deleting a file), what it changes as it is now, and what it becomes. A deletion or a merge also shows what goes with it.
- A change may use what an earlier change of the same plan adds: a release of software added in step 1.
- The changes are made one by one. The first that fails stops the plan; the editor sees what was done and what was not, and the assistant proposes a plan for the rest.
- Writing another message instead of approving drops the plan proposed.
- The editor can stop the assistant at any moment, also in the middle of a plan; the change being made is finished, the rest is not made.

## Skills
The assistant's knowledge of how a kind of task is done in this archive — finding which person a name means, finding whether software is already in the archive, adding software from a page of another site — is written down as its skills, which it reads before such a task.

## Language
The assistant answers in the language the editor wrote in. It does not fill names kept in several languages.

## Conversations
An editor has any number of conversations, each with its own history, and sees only their own. One task runs in a conversation at a time.
