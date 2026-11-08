# Typography

Text is styled only through these components; components never set font sizes, weights or line heights of their own.

- Variants: `display` (page titles), `headline` (sections), `title` (panels, cards), `body`, `bodySm`, `mono` (text on a fixed-width grid), `caption` (supporting metadata), `label` (compact labels).
- Tones: `primary`, `muted`, `strong`, `link`, `muted-link`, `link-alt`, `danger`, `inherit`. `truncate` cuts the text to one line with an ellipsis.
- `Text` — `variant` `body` and tone `inherit` by default; `as`: `p` (default) | `span` | `div`.
- `Heading` — `level` 1–3 sets the document outline and, unless `variant` is given, the look: 1 → `display`, 2 → `headline`, 3 → `title`. Tone `primary` by default. Every page has exactly one level 1, from `PageLayout`. Long unbroken titles wrap anywhere instead of overflowing.
- `Label` — the caption of a form control (`htmlFor`), in the `label` variant.
- `TextLink` — a link inside text: keeps the variant of the text around it and sets only the tone, `link` by default. A path of this app is navigated client-side; anything else is a plain link.
- `Text` takes `role="alert"` for a message that appears after an action, and an `id` to be pointed to.
