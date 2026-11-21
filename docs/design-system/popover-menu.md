# PopoverMenu

`PopoverMenu`, `PopoverMenuItem` — the menu of what can be done with what a page shows: a pencil button right after the text of the page title, not at the end of its line (`PageLayout` `menu`), opening a column of actions.

- The trigger is an [IconPopover](icon-popover.md) with the pencil icon, named by `label`; its window opens with `align="start"`, from the trigger's left edge, growing to the right of the title; it stops a gutter short of the viewport's right edge (`--anchored-overlay-viewport-gutter`).
- `PopoverMenuItem` is a full-width secondary `Button`: with `href` a link, with `onClick` an action. An action closes the menu before it runs, so a dialog it opens is not covered by the menu; a link closes it by leading elsewhere.
- An item that opens a dialog does not hold the dialog: the menu is gone once the item is chosen. Whoever renders the menu renders the dialog next to it and opens it from the item.
- Whoever renders the menu leaves it out when no item is allowed: an empty pencil is never shown.
