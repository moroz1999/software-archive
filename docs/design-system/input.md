# Input, Textarea

- `Input` — one-line text (`type="text"`, default) or a day (`type="date"`, ISO `YYYY-MM-DD` value).
- `Textarea` — multi-line text, `rows` visible lines (4 by default), resizable vertically.
- Both are controlled: `value` and `onChange(value)` with the new text, never the DOM event.
- `invalid` marks the control with the error border and `aria-invalid`; `describedBy` points to its error. Inside a `Field` these and `id` come from the field.
- `ariaLabel` names a control that has no label; `disabled` locks it.
