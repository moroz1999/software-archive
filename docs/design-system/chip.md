# Chip

`Chip` — a compact pill tag: a category, a role, a technical identifier.

- `variant`: `opaque` (default; colored text and border, no fill) | `filled` (tinted background) | `mono` (monospace, square corners: hardware and platform identifiers) | `mono-outline` (dashed: formats and technical parameters).
- `color` for `opaque` and `filled`: `neutral` (default) | `primary` | `danger`; role colors `artist` | `code` | `intro` for `opaque`.
- `size`: `sm` | `md` (default).
- `href` makes it a link; a path of this app is navigated client-side.
- `onRemove` gives it a remove button, named "Remove" and its text or by `removeLabel`; `disabled` locks it.
