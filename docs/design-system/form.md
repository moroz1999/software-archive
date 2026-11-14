# Form

The layout of every form: one family of components, so that all forms of the archive look and behave alike.

| Component | What it is |
|---|---|
| `Form` | The `<form>` itself: submits without a page reload and without the browser's own checks; `columns` 1 (default, a narrow column) or 2 (a wider form whose sections put two fields in a row). |
| `FormSection` | A titled group of fields: a level 2 heading with a rule under it, a landmark named by it. `columns` overrides the form's for this section: `1` for wide editors such as a picker or a list. |
| `FormField` | One control with its label above, its hint and its format error below. The control is a render function receiving `{id, invalid, describedBy}`, spread into an `Input`, a `Textarea` or a `Select`. `required` puts a star beside the label; `fullWidth` spans both columns. |
| `FormFieldset` | A group of controls under a legend, such as checkboxes or a list editor: the group is named by the legend, its error describes it. `required` and `fullWidth` as for a field. |
| `FormRow` | Two or three short fields side by side within a section; on phones one under another. |
| `FormMessage` | A message about the whole form at its top: `error` (default) is announced as an alert, `success` as a status. |
| `FormActions` | The buttons at the bottom: `align` `end` (default), `start` or `between`. Secondary buttons first, the submit button last. |

```tsx
<Form onSubmit={submit} columns={2}>
    {error !== undefined && <FormMessage>{error}</FormMessage>}
    <FormSection title={t('formSections.main')}>
        <FormField label={t('fields.title')} required fullWidth error={titleError}>
            {(control) => <Input {...control} value={title} onChange={setTitle}/>}
        </FormField>
    </FormSection>
    <FormActions>
        <Button href={backPath} color="secondary">{t('actions.cancel')}</Button>
        <Button type="submit" loading={busy}>{t('actions.save')}</Button>
    </FormActions>
</Form>
```

## Rules

- A form of a few fields has no sections; a long one is cut into sections by meaning (main, classification, relations, people).
- The star marks a value that must be given; it is not part of the name of the control.
- A business error of the whole form goes to `FormMessage` at the top, a format error or a refusal of one field under the field ([error-handling.md](../error-handling.md)).
- Deleting the entity is not a button of its form: it stands next to the page title ([page-layout.md](page-layout.md)).
- Below 576px every row and every two-column section becomes one column.
