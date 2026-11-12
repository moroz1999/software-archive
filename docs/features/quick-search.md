# Quick Search

The rules: [domain/search.md](../domain/search.md#quick-search); the read: [search.md](search.md#rest).

## Window

- "Search" in the main menu ([main-menu.md](main-menu.md)) opens a window of the field and its cross, the focus in the field; the window is named "Search" for assistive technology only. It starts empty every time; Escape, the cross and a click beside it close it, and so does going to any page from it.
- The keyword is looked for once the visitor stops typing for 400 ms, from two letters on; a shorter one asks nothing. The search follows the platform switch.
- Kinds in order: Authors, Groups, Software, each under a caption with its count ("Authors (2)"), listing its first five matches by the icon of the kind and the title, a link to its page.
- "Find all" below the matches leads to the full search of what is typed (`/search?q=…`, [full-search.md](full-search.md)); it is offered only once something is typed.
- The field is a combobox of the matches: the arrow keys choose a match round the list, the focus staying in the field; Enter opens the chosen one, or with none chosen, the full search. Typing forgets the choice.
- A kind without matches is left out; with none in any, "Nothing found.".

## Loading and errors

- Nothing is shown until the first matches arrive. The next keyword keeps the shown matches; after 100 ms they go under a spinner ([data-loading.md](../data-loading.md)).
- A failed read is a system error and goes to the error dialog ([error-handling.md](../error-handling.md)); the window shows no matches.
