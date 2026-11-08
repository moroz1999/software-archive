# Panel

`Panel` — a surface grouping related content.

- `variant`: `elevated` (default) lies on the page with a shadow; `flat` is nested in another panel with a light border.
- `surface`: `default` | `muted` | `primary` — tinted surfaces for nested panels; a flat tinted panel has no border.
- `radius`: `sm` | `md` (default) | `lg` | `xl` (cards). `padding`: `none` | `sm` | `md` (default) | `lg`.
- `title` adds a heading above the body (`titleLevel` 3 by default, or 2) and names the panel; `labelledBy` names it by a heading inside the body instead.
- `contentBleed` lets the body go edge to edge, clipped by the corners — for images on cards.
- `as`: `div` (default) | `section` | `article`.
