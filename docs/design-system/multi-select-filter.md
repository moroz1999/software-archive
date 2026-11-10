# MultiSelectFilter

`MultiSelectFilter` — many values chosen from a long list, such as the hardware of a platform.

- The chosen values stand on top as removable chips, "N selected" under the list; the order is the order of choosing.
- A filter finds options by name; groups with nothing found are left out, "Nothing found." when nothing is, and chosen values stay chosen while hidden.
- `groups` of options under their names; the list scrolls inside its own box so a long one keeps the form short.
- Controlled: `value` and `onChange(values)`. `label` names the whole choice; `hint` and `error` are shown under it and describe it for assistive technology; `disabled` locks it all.
