# OriginList

`OriginList` — where imports took an entry from, as text: for each import the source archive (muted) and the id the entry has there, the id a link to the page there when it has one ([domain/imports.md](../domain/imports.md#origin)). An entry no import brought shows "None". It takes the `origins` of an entry's details (`ExternalOrigin` of `shared/api`) and has no layout of its own: the form section it is put in places it.

```tsx
<FormSection title={t('formSections.sources')} columns={1}>
    <OriginList origins={author.origins}/>
</FormSection>
```
