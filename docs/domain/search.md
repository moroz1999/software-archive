# Search

How visitors find software, people and groups by a word, and narrow the software catalog. Being built in stages ([features/search.md](../features/search.md#stages)); the rules below are agreed.

## Quick search
- A search button in the header of every page opens a window with a field. As the visitor types, the window shows brief results in three sections: software, people, groups — each with its first few matches and the count of all of them.
- Software is found by its title and alternative titles; a person by any of their labels and the real name; a group by any of its labels and its name. Labels of nobody are not found on their own, as in the catalogs of names ([authors.md](authors.md#catalogs-of-names)).
- Search follows the platform switch ([hardware.md](hardware.md#platform-switch)).
- The full search, a section of the archive with every match and pages of its own for each section, comes later.

## Software catalog
The software list is the catalog of software, narrowed by a word and by filters:
- **year** — the year of the software's release date; software with an unknown date has none;
- **category** — choosing a category finds the software in it and in its whole subtree ([categories.md](categories.md));
- **country** — the countries of the authors: every country where an owner of a label credited on the software is located, by any of their locations. Labels of nobody bring no country.

Every filter offers only the values that the rest of the chosen conditions leave, each with the count of software over the whole result, not only the page shown. The values of a filter already chosen are counted without it, so the visitor can switch it to another value at once.

## Freshness
An edit is saved whether search works or not. Search shows it shortly after; while search is down it shows the archive as it was, and catches up once it is back.
