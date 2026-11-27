# Software

Software (a production) is a release of a program: the work itself, as it is known in the archive.

- It is for exactly one platform and has its base hardware — see [hardware.md](hardware.md).
- It has a title, a description and a release date — see [Release dates](#release-dates).
- It can be in categories of the archive's tree — see [categories.md](categories.md). The way down to software goes through the catalog and the branch of its first category from the top: Software › Games › Action › Elite; software in no category stands right under the catalog. A release is the next step under its software.
- It has credits: who made it, published it and worked on it — see [Credits](#credits).
- It has a preservation status, shown to everybody and chosen in its form: unknown (until it is known, and then not shown), missing in action (nobody has it; «Потеряна»), donationware (given away for a donation; «За донат») or in sales (still sold; «В продаже»).
- It has instructions: how it is loaded, run and played, kept apart from the description and written the same way. The instructions may be unknown.
- The title follows the common title rules (see the domain index). The description and the release date may be unknown.
- Software can be added, edited and deleted, its base hardware with it, chosen from the hardware of its platform. For now anyone can do it; access rules come later.
- Deleting software deletes what belongs only to it: its releases with their files and file trees, screenshots, maps, covers, adverts, alternative titles, languages, links, credits and where it was imported from. The people, groups and labels it credited stay, with their groups.
- It can be known under alternative titles; an alternative title can be tied to a language.
- It lists the languages it is available in.
- Languages — of the software and of its alternative titles — are chosen from the archive's fixed list of languages, each known by its code.
- Its Links section shows homepages and other external links alongside the pages of the archives it was imported from. Several programs can share a link. Each distinct address appears once on a program's page.
- A saved link is an absolute web address (HTTP or HTTPS) of one kind: general, homepage, store or video.

## Compilations

A compilation is a software entry that collects other programs. Editors choose its programs from the archive. The programs appear as cards below the compilation's releases, each leading to its own page. Visitors can also follow links to the compilations that include a program.

- A program may belong to several compilations. The same program appears once per compilation.
- A compilation may include other compilations. A program cannot include itself, directly or through other compilations.
- Removing a program from a compilation removes the membership. Deleting a program removes its memberships in both directions.

## Release dates
The release date of software and of a release is a year, or a day of that year when the day is known: most often only the year is known, and a year is never turned into a made-up day. It may also be unknown.
- It is written as the year, 1987, or as the day, 12.05.1987 (day.month.year), the same in every language.
- Dates are ordered by year, oldest first; within a year the ones known only by the year come before those with a day; unknown dates come last.
- It is labelled "Release date" wherever it is shown, and shown whole: the year, or the day. Only the card of software in a list shows the year alone ([Card](#card)).

## Releases
A release (a version) is a version of the software. It never lives apart from its software.
- It has a title, a version ("1.1"), a release type, a release date ([Release dates](#release-dates)) and a description. Everything but the type may be unknown; a release without a title is called by the title of its software.
- Release types are a fixed list: original, rerelease, adaptation, localization, bugfix, mod, crack, mia (missing in action), corrupted, compilation, incomplete, demo version, unknown.
- A release can be modified or adapted — other memory, another OS, added sound — and states its own hardware for that ([hardware.md](hardware.md#hardware-of-software-and-releases)).
- Each release has its own page, reached from the releases of its software. Releases are added, edited and deleted in the context of their software; for now anyone can do it. The version is at most 50 characters.
- A release's hardware is chosen from the hardware of its software's platform.
- A release has credits of its own, by the same rules as the credits of software ([Credits](#credits)): who made, cracked, adapted or otherwise worked on this version. They are kept apart from the credits of the software: neither is shown or counted as the other, and the authors of the software, its card, the catalog and search go by the credits of the software alone. The credits of a release are shown on its page and with it among the releases of its software, in the table and on cards.
- On the page of the software its releases can be sorted by title or year and narrowed to one type.
- A release may have one downloadable archive of any file type, up to 100 MB. Its page shows the original file name and size. The release card also shows the size, while the table offers the download link alone.
- The archive can be added or replaced while saving the release, or removed without deleting the release. Deleting the release or its software deletes the archive.
- The downloadable file is the root of a file tree. Containers such as ZIP files are opened recursively, and directories and files inside them keep their hierarchy and order. A tree is never partial: when anything inside the file cannot be read, or it nests or expands too far, the whole file stays without known contents and is shown as unreadable.
- Every file and container has an MD5 digest; directories do not. A file is marked executable by the fixed list of executable extensions of its platform. On Vector, files with the `.rom`, `.com`, `.r0m` and `.vec` extensions are executable and are never opened as containers.
- The tree is built after the release is saved, apart from it: a file that cannot be parsed never prevents saving or importing a release. Until its tree is built, the file is shown as waiting for its structure. Moving the software to another platform builds the trees of its releases again, because the platform decides what is executable. Whoever may change a release can have its tree built again on its page.
- Disk and tape images can expose their own directories and files, but nothing inside such a medium is an executable file. This remains true even when an internal name looks executable for the platform.

## Screenshots
Screenshots are part of what the archive knows, as much as its records: they are kept as uploaded, never recompressed, and are not lost with the services the archive runs on.
- Software and each of its releases can have any number of screenshots, in an order chosen by whoever may change them.
- A screenshot is a PNG, JPEG, GIF or WebP picture, judged by its content rather than by the name of its file, of at most 10 MB. Several can be uploaded at once; when one of them is refused, none is added.
- Screenshots of software are added, deleted and reordered by whoever may change the software, the order also right where the screenshots are shown; screenshots of a release, by whoever may change the release ([users.md](users.md#user-groups)). A screenshot remembers nobody who added it; it keeps the name of its original file and its size.
- Reordering names every screenshot in its new order; when the screenshots have changed meanwhile, the order is refused and has to be chosen again.
- Deleting a release or software deletes its screenshots.
- The card of software in a list shows the software's own screenshots; software without its own shows the screenshots of its releases, release by release in the order of the releases. A compilation without either shows the first two pictures of the card of each of its programs, in the order of its programs; a program that is itself such a compilation gives those of its own programs. Pointing at the picture across its width goes through them, one fading into the next.
- A release in a list without screenshots of its own shows the screenshots of its software. The page of a release, and of a compilation, shows only their own.
- A screenshot opens in a full-screen viewer. A picture that fits is enlarged only by whole multiples of its native size, while one too large to fit is reduced proportionally.

## Pictures
Besides its screenshots, software has three more galleries of its own: maps (of its levels and worlds), covers (of its box, tape or disk) and adverts (of the press and of its publishers). A release has screenshots only.
- Each gallery is kept, uploaded, ordered and deleted as the screenshots are, by whoever may change the software, with the same formats and size ([Screenshots](#screenshots)).
- They are shown on the software page after its screenshots, each left out without any; the card of software and the search show its screenshots only.
- Deleting software deletes the pictures of every gallery.

## Credits

Credits tell who made the software, who published it and who worked on it.

- The software page shows developers and publishers in dedicated fields. Someone credited with both roles appears in both fields. The Authors section shows the other credited people, groups and labels. Empty fields are omitted.
- Anyone is credited: a person, a group, or the exact label of either under which they did the work. Each of them is credited once per software, with all their roles.
- A credit has one or several roles (developer, publisher, code, graphics, music, etc.); without a chosen role it is recorded as unknown.
- The author of the original — of the work a port, a conversion or a remake is made from — is credited on it as the original author. It is a role of the software itself, so it tells where the software comes from like the others.
- A label of nobody can be credited, as imports do; editing keeps it.
- A person or a group is credited once, either by themselves or under a label of theirs, never both. Saving software or a release that credits an owner by themselves and under a label of theirs keeps only the credit under the label, with the roles of both (an unknown role giving way to a known one). When several labels of the owner are credited, the roles go to the display name of a person if it is among them, otherwise to the first of the labels by name. Attaching a label to a person or a group does the same on every software and release that credits both.
- A credit under a label is a credit of the label itself, whoever owns it: once the label is attached to a person or a group, or taken away from them, the credit stays and shows its new owner. A label is credited once, whoever it belonged to when it was credited.
- A credited name leads from software to its person or group when that owner is known, a label of nobody to its own page ([authors.md](authors.md#label-page)).
- Some roles are work on a copy of the software rather than on the software itself: release (cracking), the intro code, graphics and music, adaptation, restoring, localization, translation, guest, support and testing. Whoever had only these roles does not tell where the software comes from ([search.md](search.md)).

## Card
The card of software in a list is one link to its page: a click anywhere on it opens the software.
- It shows the screenshots ([Screenshots](#screenshots)), the title, who made the software and its release year; and the rating, once somebody voted on it ([voting.md](voting.md)).
- Who made it: those credited as its developers; for software without any, everyone credited but those who only published it. Each is named as the archive knows them: a person by their main name, a group by its name, a label by itself ([authors.md](authors.md#credits-under-a-label)); in name order.
- Who made it stands on the left, the year on the right, on one line when both fit; the year moves under the names when they are long. What is unknown is left out.

## Fresh software
The home page shows the software most recently added to the archive, newest first.
