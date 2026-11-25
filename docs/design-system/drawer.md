# Drawer

`Drawer` — a panel at the right edge of the window, over the page, for a companion the user works with while using the page under it (the AI assistant). It is not modal: no backdrop, the page stays usable, the focus stays where the user puts it.

- Labelled by its `title`; `header` holds controls beside the title, before the close cross; `footer` stays at the bottom while the body scrolls.
- Closes by its cross and by Escape pressed inside it; Escape elsewhere is left to the page.
- As wide as `--drawer-width`, the whole window when it is narrower. It stacks under dialogs, so a confirmation asked from it comes over it.
