# Tree

`Tree` — a collapsible tree of rows: an item with children has a toggle before its row that unfolds them under it, indented. Everything starts folded; which items are unfolded is the tree's own state.

- `items` — `{id, name, content, children}`: `content` is what the row shows (a link, a text), `name` names the item in the labels of its toggle.
- `ariaLabel` names the whole tree; `expandLabel(name)` and `collapseLabel(name)` name the toggle in each state, which also tells `aria-expanded`.
- Rendered as nested lists, so the rows read as a list of lists to assistive technology.
