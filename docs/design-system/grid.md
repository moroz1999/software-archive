# Grid

`Grid` — equal columns that change with the viewport; `GridItem` — a cell spanning several of them.

- Columns 1–6 per breakpoint: `desktopColumns` (default 1), `tabletColumns` (below 992px, defaults to desktop), `mobileColumns` (below 576px, defaults to tablet).
- `rows`: `auto` (default) | 1–4 equal rows.
- `gap` (default `md`), with `rowGap`/`columnGap` overriding it per axis.
- `align`, `justify`: `start` | `center` | `end` | `stretch` (default).
- `as`: `div` (default) | `ul`.
- `GridItem`: `desktopSpan` (default 1), `tabletSpan` and `mobileSpan` default to it. A cell may shrink below its content width, so truncated text inside it works.
