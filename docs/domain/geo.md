# Geo

Countries and cities, with coordinates. A city belongs to a country, or to none while its country is not known; such a city is sorted out by hand.

People, groups, labels and events can be located in a city and/or a country. A place in a city of no known country is the city alone, and is not in any country until the city gets one; a city of no known country is never given with a country. An entity can have several locations; one of them can be marked primary.

A country and a city have a name in Russian and may have one in every other language of the site. Visitors see the name in their interface language, or the Russian one when there is none in it.

Countries and cities are listed by the name visitors see. Cities are chosen within their country.

## Keeping

Administrators keep the countries and the cities in the management, as a tree: every country with its cities under it, each with how many places of people, groups and labels are in it — a country counting those in its cities too.

- A country or a city is added and edited with its names and its coordinates, a latitude and a longitude given both or not at all; coordinates are kept for later use and may stay unknown.
- The cities of no known country are listed apart, over the tree, each with how many places are in it, and are put in a country one by one; the places in such a city are then in that country.
- A city is put in a country, or in none while its country is not known. Moving a city to another country moves every place in it there: the places follow their city.
- Only a country or a city nothing of the archive is in can be deleted: a country without cities and without places in it, a city no place is in. Anything else is refused, so nothing is lost with it.
