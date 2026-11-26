# ListItem

`ListItem` — one item of a list to pick from, such as a list of conversations. A list of names is not a row of buttons: a button keeps its label on one line and centred, a title here is shown whole.

- Renders an `<li>`: put it inside a list (`Stack as="ul"`).
- `children` — the title. It is left-aligned and wraps onto more lines when long; it is never cut.
- `onPick` — called, without arguments, when the title is clicked or activated from the keyboard.
- `trailing` — controls after the title (a remove button): as wide as they need, acting on their own without picking the item.
