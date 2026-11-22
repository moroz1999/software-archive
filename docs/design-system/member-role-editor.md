# MemberRoleEditor

`MemberRoleEditor` — people, groups or their aliases, each with several roles: the members of a group, the credits of software.

- Chosen members are a grid of cards: three in a row, two on a tablet, one on a phone. A card carries the member's name, a remove button, and the chosen roles as removable chips.
- Each card has its own always-visible role field: the roles not chosen yet open under it on focus, narrowed by typing, picked by pointer or Arrow Up, Arrow Down, Enter; Escape closes them. The field stays focused and open after a pick, so roles are added one after another.
- New members are found by the field under the grid, which carries the label and a refusal of the whole list; a member already on a card is not added twice.
- Controlled: the caller owns the members, the remote search and the roles to offer.
