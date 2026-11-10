# Styles

Mandatory styling rules for the frontend. CSS Modules, design tokens and themes; see [design-system.md](design-system.md) for the component set.

## Variable hierarchy

Three levels, top to bottom:

| Level | Naming | Defined in | Used by |
|---|---|---|---|
| 1. Component variables | `--{component}-{purpose}` (`--button-primary-bg`, `--software-card-skeleton-title-width`) | the component's theme file `app/styles/components/{component}.css` | only the component's own `*.module.css` |
| 2. Semantic theme variables | `--text-color`, `--surface`, `--background-page`, `--link-color`, `--border-light`, … | `app/styles/themes.css` | only component theme files |
| 3. Base tokens | Palette: raw ramps and the semantic ramps `--primary-*`, `--secondary-*`, `--danger-*`, `--warning-*`, roles, shadows, overlay. Tokens: `--space-*`, `--gap-*`, `--padding-*`, `--font-size-*`, `--font-weight-*`, `--line-height-*`, `--radius-*`, `--border-width-*`, `--animation-*`, `--layer-*` | `app/styles/palette.css`, `app/styles/tokens.css` | only theme files |

- Light is the default; dark is set under `@media (prefers-color-scheme: dark)` — chosen automatically, no theme classes, no manual switch. Only colors and shadows differ between the themes.
- `app/styles/index.css` imports palette, tokens, themes, `components/index.css` (which imports every component theme file) and the reset, in that order.

- **A component's CSS uses only its own component variables.** Direct use of semantic or base variables (colors, spacing, font sizes, radii, shadows, …) in a `*.module.css` is forbidden.
- Component variables are mapped to semantic or base variables in the component's **theme file**: `app/styles/components/{component}.css`, where `{component}` is the kebab-case name of the module (`SoftwareCardSkeleton.module.css` → `software-card-skeleton.css`, `--software-card-skeleton-*`). This is the only place a component's look meets the theme.
- A component may declare its own variables in its module too: internal state (`--button-current-height`) and values set from code at runtime (ripple and spinner coordinates, set on the element through a ref — never through a `style` prop).
- A component variable is never used outside its owning component, and a component never sets another component's variables. Variation goes through props or the child's own variables.
- Theme files contain only custom property declarations (in `:root`, and in `:root` under `@media (prefers-color-scheme: dark)` when a mapping differs by theme) — no rules with ordinary properties.

## Component variables

- One responsibility per variable; the name says the purpose: `--{component}-{part}-{property}` (`--button-md-height`, `--panel-elevated-shadow`). Names like `--button-color-1` or `--card-custom` are invalid.
- Every component variable has an English comment in the theme file; adjacent variables of one group (a size: height, padding, font size) share one comment.
- A size without semantic meaning still gets a component variable: `--software-card-year-font-size: var(--font-size-sm)`.

## Colors and sizes

- No raw color, spacing, size, border, opacity or typography values in component CSS — everything comes through component variables. Geometric constants (`50%` for a circle, `1` for full opacity or a unit line height, `100%`, `0`, `auto`) are not design values and are allowed.
- Raw colors exist only in `palette.css` (and the shimmer and overlay tints in `themes.css`). Component theme files map colors only to palette and semantic variables.
- A component theme file may hold a raw dimension specific to that component (a button height, a dialog width, a card track width); spacing, font sizes, radii and durations come from the base tokens.
- Font sizes are whole pixels. No `em`/`rem`.
- If a needed value is missing, use the closest existing token; adding tokens to `palette.css`, `tokens.css` or `themes.css` needs the user's approval.

## Typography

- Text is styled only through the typography components (`Text`, `Heading`, `Label`) with a variant: `display`, `headline`, `title`, `body`, `bodySm`, `mono`, `caption`, `label`, and a tone.
- Custom typography (own font sizes, weights, line heights) in components is forbidden. A missing variant or tone is a question to the user.

## Layout

- Business components (pages, widgets, features, entities) do no layout in CSS: they compose `Stack`, `Inline`, `Grid`, `Inset`, `Panel`.
- `display: flex/grid`, `gap`, `padding` and layout margins are allowed only in `shared/ui` atoms.
- Negative margins and `calc(-1 * …)` are forbidden. Edge-to-edge content inside a `Panel` uses `contentBleed`.
- A parent styles only the external contour of a child (`margin`, `min-/max-width`, `align-self`, `flex-grow`) through its `className`.
- If a layout cannot be built from the existing components, ask the user before extending them.

## CSS Modules

- Every component has its own `{Component}.module.css` next to it (components of one family may share one: `Typography.module.css` for `Text`, `Heading`, `Label`). Class names and keyframes are camelCase (`gapMd`, `rippleExpand`); prop-dependent variants are mapped explicitly (`{md: styles.gapMd, …}`), never built from strings.
- `npm run css:types` generates a typed `*.module.css.d.ts` next to each module (git-ignored; `typecheck`, `build` and `check` run it first).
- No global classes; global CSS lives only in `app/styles/`. No inline `style` attributes.

## Breakpoints

Layout components switch at two widths: tablet below 992px, mobile below 576px. Media queries cannot read custom properties, so these two values are written in the modules of the layout components that react to them. Markup that changes with the screen, not only its look, asks `useMediaQuery` (`shared/lib/screen`), which holds the phone width as `MOBILE_QUERY`.

## Automatic checks

A local Stylelint plugin (`frontend/stylelint/plugin.js`, with its own tests) enforces the hierarchy, next to the standard rules listed in [frontend.md](frontend.md#static-checks):

| Rule | Checks |
|---|---|
| `archive/component-variables` | A `*.module.css` uses and declares only its own `--{component}-*` variables, and every one it uses is declared in the module or in its theme file |
| `archive/theme-file` | Palette, tokens, themes and component theme files hold only custom properties in `:root`, light by default and dark under `@media (prefers-color-scheme: dark)`; a component theme file declares only its own prefix, each variable commented |
| `archive/theme-imports` | `components/index.css` imports every component theme file, and only existing ones |

Component theme files are also checked for raw colors: no hex, named colors or color functions.
