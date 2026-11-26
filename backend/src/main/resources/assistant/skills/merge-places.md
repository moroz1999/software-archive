---
name: merge-places
description: What each merge and conversion of countries and cities does, and which one a case needs. Load it before merging or moving a country or a city.
---

Every one of these changes is final: nothing undoes it. Read both entries before, and say in the plan why they are one.

- `merge_city`: two cities of one country, one name written otherwise (or a city of no known country that is a city of a country). Every place in it goes to the other city and its country; its names go with it. Keep the one named correctly.
- `merge_country`: two countries that are one. Its cities and every place in it go to the other one.
- `put_city_in_country`: a city of no known country whose country is known, or a city put in a wrong country. Every place in it follows it.
- `turn_city_into_country`: a city of no known country that is a country itself (an import took a country for a city). It becomes a country with its names, every place in it is in that country. A country of the same name already in the archive is then a duplicate: merge it (`merge_country`).
