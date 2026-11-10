# Field

`Field` — a form control with its `label` above, its `hint` and its format `error` below. The control is given as a render function receiving `{id, invalid, describedBy}`, spread into an `Input` or `Textarea`:

```tsx
<Field label="Title" error={titleError}>{(control) => <Input {...control} value={title} onChange={setTitle}/>}</Field>
```

The label points to the control, and the hint and the error become the control's accessible description. A business error of the whole form is shown at the form's top, not in a field ([error-handling.md](../error-handling.md)).
