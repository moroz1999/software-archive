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
- Every run leaves a report for the administrator: what it imported, whether it went through to the end or was stopped (by a failure of the whole run or a restart of the archive), how many programs it added, found already there, left out and failed, and what it does now: reading the list of programs, or which program of how many it is at, when the source tells how many there are.
- The report keeps a log of the run: every step it took, one line after another in the order it went and with the time of each, growing while the run goes on, so the administrator follows the run as it goes:
  - every page of the list of programs read, of how many pages, and how many programs it lists;
  - every file of a program downloaded: its archive and its screenshots, with their names and sizes;
  - then what the program came to: added; already there, with what was added to it — its description, release date, preservation status, categories, authors, screenshots — or that there was nothing to add; left out, with the section or the category of the source it was left out by; or failed, and why;
  - after it, every author the program took for the first time: a person, a group or a label created, or a person or a group of the archive found to be them; and every city and country it added.
- The reports of the runs made before the log became their logs as they were, without the time of their lines, which nobody wrote down.
- What a program that failed brought — its authors and places — is not in the log: it never reached the archive. The files it downloaded before it failed are.

## Software
- A program is matched by the files of its release, compared by MD5. Which files make a release is told by the platform: on Vector, its executable files (ROM, COM, R0M, VEC), floppy images and tape recordings (CAS and WAV). A release of the archive whose such files are exactly the files of the source's release — all of them, none more — is the same release, and the program is the same software, whichever source it came from; a ZIP of the archive holding the same ROMs as a ZIP of the source is the same release. Software that already came from the same source, under another id there, is never the match: two entries of one source stay two programs, even with the same files. Anything else is another program and is added as new. Duplicate software made this way is merged by hand later.
- A person the source names by a nickname is credited under that nickname, never as the person: under which nickname a program came out is history worth keeping, even when the nicknames are one person's. Only once the person has no label of that name any more are they credited themselves.
- Authors of a program that the archive's software does not credit yet are added to its credits. Duplicates this makes — one subject under two labels from two sources — are visible and are fixed by hand later.
- The categories of a source are matched to the categories of the archive; a category of a source can also stand for hardware of the archive.
- The description of the source fills the description of the software only when it has none; so does the preservation status of the source, only while the status of the software is unknown.
- A screenshot the software already has is not taken again; any other screenshot of the source is added to the program. Screenshots of a source are PNG pictures of their own size, so a screenshot of the same size is the same one: an occasional mistake does no harm. The archive keeps what it knows of an imported screenshot, its original file name and size.

## Authors
- An author whom the source does not tell to be a person or a group is a label of nobody, sorted out by hand later; each source tells by rules of its own when an author is a person or a group.
- An author page of the source gives data about the author, such as the full name; it goes to the author in the archive by the rule of empty fields.
- An author the archive has nothing of from this source yet may be one the archive already has, from another source or added by hand. Before a new one is created, the archive is searched, and an author found is remembered as that author of the source from then on:
  - A person is the archive's person only when both the full real name, of at least two words, and the nickname are the same, whatever their case: "S.E.S., Ермолаев Сергей" is the S.E.S. Ермолаев Сергей of the archive. The nickname alone or a short name ("Сергей") is too little: people share nicknames.
  - A group is the archive's group of the same name, or with a label of that name, compared by letters and digits alone, whatever their case: "E.B.O. Soft" is EBO SOFT, but SOFT is not SOFT123. Groups almost never share a name, so a label of nobody a source names is taken for such a group too.
  - The place must not contradict: when both the source and the archive tell where the author is from, they share a city or a country. A place the archive does not know is another place.
  - When two of the archive match, neither is taken: a new author is created, and the duplicate is fixed by hand.
  - A label of nobody is never taken for a person of the archive.
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
- An author page that gives a nickname apart from the name is a person: the name is the real name, the nickname is the person's label. When the two are the same, the author is a group. An author page without a nickname is a label of nobody under that name, located in its place. "Неизвестный Автор" credits nobody, and its place "Нигде" is no place.
- The place an author is from is written as it comes, in Russian: mostly a city, sometimes a country ("Japan") or a city with its region. A place named as a country of the archive is that country; any other is a city of that name, and a new city has no country, since the site does not tell it.
- A group made of an author there has no group type: the site does not tell it.
- Some categories there are hardware of the archive. "tape" and "disk" are tape and the floppy disk. "krista-2", "pk-6128c" and "start-1200" are the Krista-2, the PK-6128C and the Vector Start-1200, the base computer of the program in place of the Vector-06C. "ay", "beeper", "vi53-music", "joystick_p", "joystick_s", "hdd", "kvaz", "barkar" and "z80" are the AY, the beeper, the VI53 timer sound, the joysticks P and S, the hard disk, the quasi-disk, Barkar's quasi-disk and the Z80 processor, which the program uses.
- "missing" is no category: the program is missing in action, its preservation status. "pc" is software for the PC and is left out. "magazine", "document" and "catalogue" are paper, not software of the archive: a card in any of them is not taken; nor are the marks that are not a genre ("3D", "basic", "beta", "event", "isometric", "multiplayer", "msx", "zx", "pron").
