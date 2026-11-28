# Hardware

What software runs on.

## Platform
A family of computers: RK-compatible, Orion, Specialist, PK8000, Korvet, Vector, Lviv, Okean, Bashkiria, BK, Agat, XT clones and MSX.
- XT clones are one platform for the Soviet machines compatible with the IBM PC XT: Poisk, ES-1840, Iskra-1030, Neuron and the like.
- How MSX is kept (as the Aleste) is an open question; for now it is a platform as it is.

A platform holds all models and clones of its family. BK is one platform for every BK; a model or a clone is hardware inside it, not a platform of its own.

The platforms are the families the archive plans for; their hardware is added only once it is confirmed. For now only the Vector family ([The Vector family](#the-vector-family)) and the BK family ([The BK family](#the-bk-family)) have hardware; every other platform has none yet.

## Names
Every platform and every item of hardware is named in English and in Russian. Visitors see the name in their interface language, or the Russian one when there is none in it, and lists follow that name. In Russian the MSX platform is called «Ямаха КУВТ».

## The RK family
Radio-86RK, Mikrosha, Apogey and Partner are one platform, RK-compatible, for finding things in the archive, and different machines for running them.
- They share the KR580VM80A processor (a Soviet Intel 8080), related video circuits and part of the calls of their firmware, the Monitor. A program that uses only those calls runs as one binary on several machines; a game that works the screen, the keyboard or the sound directly depends on the machine.
- Radio-86RK: built by radio amateurs from a scheme published in 1986; 16 KB or 32 KB of memory, and the screen buffer of the two lies at different addresses. A monochrome text screen with pseudographics.
- Mikrosha: the factory-made relative of the RK; 32 KB, another character generator, a timer for sound, other device addresses and its own tape format.
- Apogey BK-01 and BK-01C: a better equipped factory machine on the base of the RK, about 56 KB for the user and three-channel sound; the BK-01C shows colour. Not to be confused with the Elektronika BK, a PDP-11 machine of the BK platform.
- Partner 01.01, with its colour module.
- Each machine is to be hardware of the platform once it is confirmed; the memory variant of the RK and the colour Apogey are told apart because they decide whether software runs and how it looks.
- The same program can exist as two tape records, one in the format of the RK and one of the Mikrosha; they are releases of one software. A different character generator or colour mode can make the same bytes look different.
- Specialist and Orion stay platforms of their own. Software belongs to exactly one platform, so a program confirmed to run on them as well is not shown there yet; that is an open question.

## The Vector family
The Vector platform is the family of computers compatible with the Vector-06C.
- Computers (models and clones): the Vector-06C (Вектор-06Ц), the Krista-2 (Криста-2), the PK-6128C (ПК-6128Ц) and its forerunner the Vector Start-1200 (Вектор Старт-1200); they are partially compatible.
- Storage: tape, the floppy disk, the hard disk, the quasi-disk (a RAM disk) and Barkar's quasi-disk.
- Sound: the AY chip, the beeper and the sound of the VI53 timer.
- Controls: the joysticks P and S.
- Expansion: the Z80 processor in place of the 8080.

## The BK family
The BK platform is the family of the Elektronika BK, PDP-11 machines.
- Computers (models and clones): the BK-0010 (БК-0010), the BK-0010-01 (БК-0010-01), the BK-0011 (БК-0011), the BK-0011M (БК-0011М) and the AZBK (AZБК).
- Expansion: the SMK-512 controller (СМК-512), memory up to 512 KB and hard disks.
- ROM firmware: BASIC (Бейсик) and FOCAL (Фокал), the interpreters in the ROM of the BK that a program of theirs needs.
- A program for the BK-0010 that also runs on the BK-0011M has both computers.

## Hardware
A flat list kept in the archive as a reference book. Every item has:
- a name — "Sound card 1";
- a code — `chip123`;
- a group — sound;
- the platforms it belongs to — `vector`, at least one. One item can belong to several platforms: the AY chip was used with both the Specialist and the ZX Spectrum.

The groups are a reference book of their own, each with a code and a name, in an order of their own. The archive started with computers (models and clones), storage, dos, sound, controls and expansion; ROM firmware came with the BK.

Hardware is shown grouped by its group, the groups in their order.

## Keeping the reference book
Administrators add, edit and delete groups and hardware.
- A code is lower-case Latin letters and digits, words joined by a hyphen. It is given once, when the group or the item is added, and never changes: links and the search know things by it. Two groups, or two items, never share a code.
- A name is given in Russian at least, in any other language of the site besides, at most 255 characters each.
- A new group goes to the end of the order.
- A group with hardware in it cannot be deleted; hardware of any software or release cannot be deleted.
- Hardware cannot leave a platform while software of that platform, or one of its releases, has the hardware: that hardware would belong to another platform than its software.
- Platforms themselves are not kept here: hardware only chooses among them.
- Hardware moved to another group is found under that group from then on, in the catalog and in search.

## Hardware of software and releases
- Software is released for its base hardware. That hardware is attached to the software.
- A release can be modified or adapted: other memory, another OS, added sound, anything else. Its differences are attached to the release.
- A release replaces whole groups. A group the release has replaces the same group of the software entirely; every other group comes from the software. Sound replaced → computers, controls and the rest from the software, all sound from the release.
- A release without hardware shows the hardware of its software.

## Platform switch
One switch at the top of every page picks a platform for the whole archive: every list and catalog follows it.
- Nothing chosen: all software, people and groups of every platform.
- A platform chosen: only the software of that platform, and only the people and groups linked to it.

Software always has exactly one platform. It cannot change platforms while any of its releases has hardware: that hardware belongs to the old platform. Its own hardware is chosen anew together with the new platform. Everything else gets its platforms through software: a person or a group is linked to a platform by credits on software of that platform.
