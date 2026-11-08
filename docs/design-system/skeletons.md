# Skeletons

`Skeleton` — a bone: a placeholder block hidden from assistive technology, with a shimmer sweeping over it.

- It is a building block: every data-driven component gets a concrete skeleton of its own layout (e.g. `SoftwareCardSkeleton`), built from the same layout components as the real one, so content replaces the skeleton without jumps.
- The concrete skeleton sizes its bones (width, height, aspect ratio) through `className` with its own component variables.
- `animated` (default on) — the shimmer; `inline` — sits in a line of text.
- Bones outside the viewport are not painted, so long skeleton lists cost nothing off screen.
- The shimmer stops under `prefers-reduced-motion`.
