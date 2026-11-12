# Quick Search

The rules: [domain/search.md](../domain/search.md#quick-search); the read: [search.md](search.md#rest).

## Window

- "Search" in the main menu ([main-menu.md](main-menu.md)) opens a window with the field, the focus in it. The window starts empty every time; Escape closes it, and so does going to any page from it.
- The keyword is looked for once the visitor stops typing for 300 ms; a blank one asks nothing. The search follows the platform switch.
- Sections in order: Software, Authors, Groups, each titled with its count ("Authors (2)") and listing its first five matches as links to their pages. A person shows the real name muted next to the main name when it is another one. "All software (12)" next to the Software title leads to the software catalog of the keyword (`/software?q=…`, [software-list.md](software-list.md)).
- A section without matches is left out; with none in any, "Nothing found.".

## Loading and errors

- Nothing is shown until the first matches arrive. The next keyword keeps the shown matches; after 100 ms they go under a spinner ([data-loading.md](../data-loading.md)).
- A failed read is a system error and goes to the error dialog ([error-handling.md](../error-handling.md)); the window shows no matches.
