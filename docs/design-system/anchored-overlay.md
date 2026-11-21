# AnchoredOverlay

`AnchoredOverlay` — the shared overlay primitive: content under an anchor element, such as the results of an autocomplete or the window of an `IconPopover`. It follows the anchor while the page scrolls and resizes.

- It is a manual popover shown in the browser's top layer, so it is above the page and any `Dialog` on it, with no z-index. Overlays in the top layer stack in the order they open: the newest is on top, wherever it is opened from.
- Manual: the component that opens it decides when it closes (Escape, a press outside), the browser does not.
- `align="stretch"` (default) is as wide as the anchor from its left edge, for lists under a field; `align="end"` is as wide as its content, its right edge under the anchor's, for windows under a button at the end of a line; `align="start"` is as wide as its content from the anchor's left edge, for a button whose window grows to the right, and stops a gutter short of the viewport's right edge (`--anchored-overlay-viewport-gutter`).
- jsdom has no Popover API: `vitest.setup.ts` stands in for it, so an overlay is visible in tests only once it is shown as a popover.
