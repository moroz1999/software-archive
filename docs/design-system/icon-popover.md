# IconPopover

`IconPopover` — an icon button of the header with a window of contextual content under it, such as the account menu.

- The trigger is a transparent square `Button` named by `label`, with `aria-haspopup="dialog"` and `aria-expanded`; the window is a `dialog` named by the same `label`.
- The window opens through [AnchoredOverlay](anchored-overlay.md) with `align="end"`: under the trigger, its right edge under the trigger's, as wide as its theme says and never wider than the viewport.
- Controlled: `open` and `onOpenChange(open)`. It asks to close on the trigger, on Escape (the focus goes back to the trigger) and on a press outside both; a press inside keeps it open. Whoever holds `open` may close it for other reasons too (going to another page).
