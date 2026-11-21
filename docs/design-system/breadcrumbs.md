# Breadcrumbs

`Breadcrumbs` — the way from the top of the site down to the page, in small muted text over the page title (`PageLayout` `breadcrumbs`).

- `steps` go from the top down; each is a `label` and, above the page, an `href` it leads to. The last step is the page itself: named without a link and marked as the current page, whatever `href` it has.
- Steps are an ordered list inside a navigation landmark named "Breadcrumbs" ("Навигационная цепочка"). The `›` between them is drawn by the stylesheet, so a step reads as its name alone.
- Long ways wrap onto the next line; nothing is cut.
- Theme: `--breadcrumbs-*` (`app/styles/components/breadcrumbs.css`): the text size, the gap around the separator, the colors of the separator and the page itself.
