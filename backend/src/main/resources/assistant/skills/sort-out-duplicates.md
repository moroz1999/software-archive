---
name: sort-out-duplicates
description: Finding and sorting out duplicates among people, groups, labels or software, e.g. on the list the editor is looking at.
---

- The list on the editor's page: read it with the tool and the arguments the page context names (`list_authors`, `list_groups`, `search_software`, `search`, `list_latest_software`), page by page if the editor asks for more than the page shown.
- Candidates are entries whose names differ only in letter case, spaces, punctuation, a transliteration (Cyrillic and Latin) or a common misspelling. Also look for each name with `find_names` and `search`: a duplicate may stand on another page.
- A same name is only a candidate. Read both entries (`get_author`, `get_group`, `get_label`, `get_software`) and compare:
  - people: real names, locations, groups, the software and releases they are credited on, who they worked with;
  - groups: types, locations, members, works;
  - labels: owners, locations, memberships, works;
  - software: platform, release date, credits, releases and their files (`get_release`: the same MD5 digest is the same file).
- Duplicates: the facts agree and nothing contradicts. Different people, places or times mean namesakes: leave them, and say why.
- Facts too thin to decide either way: list the pair for the editor and ask. Never merge on names alone.
- Nothing of either entry may be lost: what one has, the kept one gets.
- A duplicate label is merged into the person, the group or the label it duplicates (`merge_label_into_author`, `merge_label_into_group`, `merge_label_into_label`); its credits go with it. A label of nobody that is a person or a group not yet in the archive: `make_author_of_label`, `make_group_of_label`.
- Propose one plan per duplicate, each saying what you compared and why they are one.
- Two people, two groups or two software cannot be merged yet: report them with your evidence instead of a plan.
- Answer with a short table: each pair found, the verdict (duplicate, namesake, unsure) and why.
