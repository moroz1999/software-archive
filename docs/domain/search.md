# Search

How visitors find software, people and groups by a word, and narrow the software catalog. Being built in stages ([features/search.md](../features/search.md#stages)); the rules below are agreed.

## Quick search
- A search button in the header of every page opens a window with a field. As the visitor types, the window shows brief results by kind: people, groups, software — each with its first five matches, best first, and the count of all of them. From the window the visitor goes to the full search of the same word.
- Software is found by its title and alternative titles; a person by any of their labels and the real name; a group by any of its labels and its name. Labels of nobody are not found on their own.
- A person or a group found by any of their names is shown under the main name, a group under its name ([authors.md](authors.md#main-name)); a person with the real name next to it, unless it is the main name itself.
- Search follows the platform switch ([hardware.md](hardware.md#platform-switch)).

## Full search
- A section of the archive with every match of the word, by the same rules of finding as the quick search.
- The matches are one list: people, then groups, then software, the best matches first within each kind, cut into pages of fifty. A page shows the kinds that fall on it, each titled with the count of all its matches.
- The visitor chooses which kinds to search; all of them at first. Another choice starts from the first page.
- With no match at all, the visitor is told nothing was found.

## Software catalog
The software list is the catalog of software. Visitors go through it by the categories: the catalog of a category holds the software in it and in its whole subtree ([categories.md](categories.md)); the whole tree is offered, without counts. A category is where the catalog is, not a filter: the word and the filters keep it, and going to another category keeps them.

Within a category the catalog is narrowed by a word and by filters:
- **year** — the year of the software's release date; software with an unknown date has none;
- **country** — where the software comes from: every country where a person or a group credited on it, by themselves or by a label, is located, by any of their locations. Only the roles of the software itself count: who only cracked, adapted, translated or helped with it brings no country ([software.md](software.md#credits)). Labels of nobody bring no country.
- **city** — chosen within the chosen country only: the cities of those same locations that are cities of it. Without a country there is no city to choose.
- **platforms** — while the switch shows every platform ([platform-switch.md](../features/platform-switch.md)): software of any of the platforms checked. With one platform in the switch the catalog is of it alone and offers no platforms.
- **hardware** — the hardware of the software itself and of every one of its releases ([hardware.md](hardware.md)). The items checked in one group, as two computers, are any of them; every group checked is needed, and the platforms are a group of their own: "BK, Vector-06C and AY-3-8910" finds software for the BK or the Vector-06C that has the AY-3-8910. The hardware offered is only of the platform in the switch, since the software found is; a code the archive has no hardware for finds nothing.

A fact of software that the catalog goes through or is narrowed by — its year, each of its categories and the categories above them — leads from the page of the software to the catalog of that value alone. A release has no such facts: its own year is not the year of its software.

Every filter offers only the values that the rest of the chosen conditions leave, each with the count of software over the whole result, not only the page shown. The values of a filter already chosen are counted without it, so the visitor can switch it to another value at once.

## Freshness
An edit is saved whether search works or not. Search shows it shortly after; while search is down it shows the archive as it was, and catches up once it is back.
