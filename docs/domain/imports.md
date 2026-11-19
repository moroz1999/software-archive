# Imports

The archive takes in software from other archives so that it does not have to be typed in again.

## One import, many sources
- Every source archive has its own parser: it only reads the source and gathers what it finds there. Everything a parser gathers goes into one import, which takes it in by the same rules for every source.
- The archive is the reference. An import adds what the archive does not have yet and never rewrites what it has: a field of the archive is filled from a source only while it is empty.
- An import can run again at any time: every run takes in the additions the source has got since, and leaves what the archive already has as it is, even when the source has changed it; what the source has deleted stays in the archive.

## Origin
- Whatever an import adds remembers where it came from: the source archive and the id the entry has there (vektor-06c.ru, 524). A name that has no id there, such as an author, is known by the name itself — the label text as the source gives it.
- Imported software links back to its page in the source archive, so anybody can compare it with the original.
- Whatever an import added and the archive deleted since — software, a label, a person, a group — is forgotten with it: the archive keeps no trace of where a deleted entry came from, and the next run meeting it takes it again, as new. Deleting software forgets only the software: the authors and labels it credited stay, and so does where they came from. Deleting a person, a group or a label forgets only them, with what is known of them, such as their locations: the software they were credited on, its releases and the places they were from stay.
- An author is remembered by its origin, so a later import of the same author from the same source first looks for whom it belongs to in the archive, and creates nothing new when it is there: it credits that subject. A label IVAN A imported from a source and made into the person Ivan Andreev in the archive stays remembered as Ivan Andreev; the next run meeting IVAN A on that source credits Ivan Andreev, a program added there under IVAN A since included.
- What an import adds is added by one fixed user of the archive, the importer.
- Only an administrator starts an import, from the management of the archive: a whole source archive or one of its sections. Only one runs at a time, so that two runs never add the same entries.
- Every run leaves a report for the administrator: what it imported, whether it went through to the end or was stopped (by a failure of the whole run or a restart of the archive), what it added, what was already there or left out, and what failed and why. The report grows while the run goes on.

## Software
- A program is matched by the files of its release, compared by MD5. Which files make a release is told by the platform: on Vector, ROM files, floppy images and tape recordings (CAS and WAV). A release of the archive whose such files are exactly the files of the source's release — all of them, none more — is the same release, and the program is the same software, whichever source it came from; a ZIP of the archive holding the same ROMs as a ZIP of the source is the same release. Software that already came from the same source, under another id there, is never the match: two entries of one source stay two programs, even with the same files. Anything else is another program and is added as new. Duplicate software made this way is merged by hand later.
- Authors of a program that the archive's software does not credit yet are added to its credits. Duplicates this makes — one subject under two labels from two sources — are visible and are fixed by hand later.
- The categories of a source are matched to the categories of the archive; a category of a source can also stand for hardware of the archive.
- The description of the source fills the description of the software only when it has none; so does the preservation status of the source, only while the status of the software is unknown.
- A screenshot the software already has is not taken again; any other screenshot of the source is added to the program. Screenshots of a source are PNG pictures of their own size, so a screenshot of the same size is the same one: an occasional mistake does no harm. The archive keeps what it knows of an imported screenshot, its original file name and size.

## Authors
- An author page of the source gives data about the author, such as the full name; it goes to the author in the archive by the rule of empty fields.
- A city or a country of an author or a group found in a source is looked for among the cities and countries of the archive by their names in all languages. One that the archive does not have is created, with its name in one language. What a place of a source is, a city or a country, and the language of its name, is told by each source.

## vektor-06c.ru
The archive of software for the Vector-06C.
- A program there is software of the Vector platform for the Vector-06C, with exactly one release of the original type; its ZIP archive is that release's archive, taken whole.
- Its picture is the screenshot of the software, unless it is the picture the site shows when it has none ("Скриншота нет").
- A year "1987 / 2026" (or "1987 - 2026") is a work of 1987 released for the Vector in 2026: the first year goes to the software, the second to the release. A single year is the year of both.
- Its authors are one line of names apart by commas. Each name is taken whole as a label of nobody ("Городецкий И. /ivagor" is one label), credited with an unknown role: the site does not tell people from groups or nicknames from real names, so the labels are sorted out by hand later. The marks of an unknown author ("не известен", "Неизвестный Автор", "-") credit nobody.
- The storage of the release is told by the files of its archive: a disk image is a floppy, a ROM file or a recording is a tape.
- Each section of the site goes to one category of the archive. The utilities for the PC are left out: they are not software of the Vector and will get a catalog of their own.

## caglrc.cc/scalar
"Базис", the card index of software for the Vector-06C.
- A program there has a title, a year, categories, a description, screenshots, its authors and its file.
- Its authors have ids and pages of their own there, with the full name, the nickname and the place the author is from; an author is remembered by that id.
- An author page that gives the name of the author apart from the name of the label is a person: the full name is the real name, the label is the person's. When the two names are the same, the author is a group.
- The place an author is from is a city, its name written in Russian; the site does not tell its country, so a new city goes to the country "Unknown".
- A group made of an author there has no group type: the site does not tell it.
- Some categories there are hardware of the archive: "tape" is tape, "disk" is a floppy disk, "krista-2" is the Krista-2, the base hardware of the program in place of the Vector-06C. "pc" is software for the PC and is left out; "missing" is not taken.
