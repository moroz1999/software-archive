# Table

`Table` — rows of data under named columns, rows edge to edge; it scrolls sideways when wider than its place.

- Built from `TableHead`, `TableBody`, `TableRow`, `TableHeaderCell`, `TableCell`; `label` names the table, `align` places a cell's content.
- `fixed` lays the columns out by the widths their header cells set (`className` of `TableHeaderCell`), not by their content: a skeleton under the same header lines up with the data that replaces it.
- A column that sorts gets `sort` (how the rows are sorted by it now) and `onSort`: its name becomes a button with an arrow, the order told by `aria-sort`. The rows are sorted by whoever owns them.
