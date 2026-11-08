# Button

`Button` — the only interactive control for features and pages.

- `size`: `xs` | `sm` | `md` (default). `color`: `primary` (default) | `secondary` | `danger` | `transparent` (low-emphasis trigger) | `outlined`.
- Shape: `square` — icon-only, as wide as high; `round` — square with fully rounded corners; `block` — the full width of its container.
- Without `href` it is a `<button type="button">`; `type="submit"`/`"reset"` only when asked. `onClick` is called without arguments: DOM events never leak into callers.
- With `href` it is a link that looks like a button. A path of this app (`/software`) is navigated client-side; anything else is a plain link. `target="_blank"` always adds `noopener noreferrer` to `rel`.
- `disabled` blocks the button; a disabled link is taken out of the tab order and ignores clicks.
- `loading` (buttons only) while the button's action runs: locked and `aria-busy`.
- `ariaLabel` names icon-only buttons.
- A press plays a ripple from the pressed point; it is skipped under `prefers-reduced-motion`.
- `className` — external contour only.
