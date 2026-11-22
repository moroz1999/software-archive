# Geo

Countries and cities, with coordinates. A city belongs to a country, or to none while its country is not known; such a city is sorted out by hand.

People, groups, labels and events can be located in a city and/or a country. A place in a city of no known country is the city alone, and is not in any country until the city gets one; a city of no known country is never given with a country. An entity can have several locations; one of them can be marked primary.

A country and a city have a name in Russian and may have one in every other language of the site. Visitors see the name in their interface language, or the Russian one when there is none in it.

Countries and cities are listed by the name visitors see. Cities are chosen within their country.

## Keeping

Administrators keep the countries and the cities in the management, as a tree: every country with its cities under it, each with how many places of people, groups and labels are in it — a country counting those in its cities too.

- A country or a city is added and edited with its names and its coordinates, a latitude and a longitude given both or not at all; coordinates are kept for later use and may stay unknown.
- The cities of no known country are listed apart, over the tree, each with how many places are in it, and are put in a country one by one; the places in such a city are then in that country.
- A city of no known country may turn out to be a country itself: an import takes every place it does not know as a country for a city. Such a city is made a country of its names and coordinates; every place in it is then in that country alone, primary as it was, and the city goes. A city of a country is never made one, nor a city an old record of a label names, as that record would have nowhere to go. A country of the same name already in the archive is not looked for: the two show up as possible duplicates.
- A city is put in a country, or in none while its country is not known. Moving a city to another country moves every place in it there: the places follow their city.
- Only a country or a city nothing of the archive is in can be deleted: a country without cities and without places in it, a city no place is in and no old record of a label names. Anything else is refused, so nothing is lost with it.
- Duplicates are merged by hand. Countries, and cities of one country, named alike in any language — letter case, "ё" and "е", spaces and hyphens aside — are shown as possible duplicates, a city of no known country with the cities of every country named like it; nothing is merged by itself.
- A country or a city is merged into another one, never into itself, and the one merged goes with its names and coordinates; the one it goes into keeps its own. Nothing else of it is kept.
  - A merged city has every place in it, and every old label record of it, put in the city it goes into and that one's country.
  - A merged country has its cities, and every place in it, put in the country it goes into.
  - An owner who then has one place twice keeps it once, primary when either was.
