# Frontend

React 19, TypeScript, Vite, React Router, CSS Modules.

## Feature-Sliced Design (strict)

Layers under `frontend/src`, top to bottom:

| Layer | Holds |
|---|---|
| `app/` | Entry point, providers, the router (one route per page, each with its preload loader), global styles (palette, tokens, themes, component theme files, reset) |
| `pages/` | One slice per route; composes widgets and features |
| `widgets/` | Large self-contained blocks of a page |
| `features/` | User actions that bring business value |
| `entities/` | Business entities: their model, API and display components |
| `shared/` | Domain-agnostic code: the design system (`shared/ui`), API client, libs, config |

Rules:
- A module imports only from layers strictly below its own. Never from the same layer or above.
- Slices of the same layer never import each other.
- Every slice (`pages/software`, `features/software-list`, …) is a folder with a public API in `index.ts`. Code outside the slice imports only through that file — never by deep path.
- Inside a slice, code is split by segments: `ui/`, `model/`, `api/`, `lib/`, `config/`.
- `app/` and `shared/` have no slices, only segments.
- Domain knowledge never appears in `shared/`.
- The rules are enforced by Steiger: `docker compose exec frontend npm run lint:fsd`. `fsd/insignificant-slice` is off: a young slice used by one page is not merged back into it. `fsd/repetitive-naming` is off: slices are named `{entity}-{action}` (`software-list`, `software-edit`), so they share the entity prefix.

## Static checks

All of them must pass with zero warnings; a rule is never disabled inline to get a change through.

- **TypeScript** — strictest mode (`strict` plus every extra check: `exactOptionalPropertyTypes`, `noUncheckedIndexedAccess`, `noPropertyAccessFromIndexSignature`, …) in both `tsconfig.app.json` and `tsconfig.node.json`. `docker compose exec frontend npx tsc -b`.
- **ESLint** — `typescript-eslint` `strictTypeChecked` + `stylisticTypeChecked`, React, React Hooks, `jsx-a11y` strict, and for tests Vitest `all` + Testing Library. `docker compose exec frontend npm run lint` (fails on any warning).
- **Stylelint** — `stylelint-config-standard` + CSS Modules + the design system rules (see table below) + the local plugin for the variable hierarchy ([styles.md](styles.md#automatic-checks)). `docker compose exec frontend npm run lint:css`.
- **Steiger** — FSD rules, see above.
- Everything at once, as before a hand-off: `docker compose exec frontend npm run check` (types, ESLint, Stylelint, Steiger, tests).

Project rules enforced by ESLint:

| Rule | Check |
|---|---|
| Named exports only (tool configs excepted) | `no-restricted-syntax` |
| No inline `style` | `react/forbid-dom-props` |
| No native `<button>`/`<a>` outside `shared/ui` | `react/forbid-elements` |
| No type assertions; `as` only in `model` segments and `shared/lib`, where value object constructors brand raw values | `@typescript-eslint/consistent-type-assertions` |
| `fetch`/`XMLHttpRequest` only in `shared/api` | `no-restricted-globals` |
| Only `*.module.css` imports outside `app/` | `no-restricted-imports` |

Design system rules enforced by Stylelint:

| Rule | Check |
|---|---|
| Colors, spacing, borders, radii, shadows, sizes, opacity, typography and z-index only through `var(--…)`; no hex or named colors | `scale-unlimited/declaration-strict-value`, `color-no-hex`, `color-named` |
| A component uses only its own `--{component}-*` variables, declared in its theme file | `archive/component-variables` |
| Theme files hold only commented custom properties, light and dark by media query; all are imported | `archive/theme-file`, `archive/theme-imports` |
| camelCase class and keyframes names | `selector-class-pattern`, `keyframes-name-pattern` |
| No `display: flex/grid`, `gap`, `padding` outside `shared/ui` and `app/` | `declaration-property-value-disallowed-list`, `property-disallowed-list` |
| No negative margins anywhere | `declaration-property-value-disallowed-list` |
| No `:global` outside `app/` | `selector-pseudo-class-disallowed-list` |

## Data loading

- HTTP goes only through `shared/api`: `getJson` and the response readers (`readObject`, `readString`, `parsePage`, `mapResponse`, …) that turn `unknown` JSON into typed values without type assertions.
- Each entity's `api` segment fetches and maps its DTOs into the entity model; a response that does not fit the model is a `SystemError` of kind `response`.
- Blocks load data with `useRequest(request)`, where `request` is a view request declared at module level, so the route can preload it.
- When and how blocks load — route preload with a 300 ms window, lazy loading by viewport, skeletons on first load, spinners for pagination and filters — is defined in [data-loading.md](data-loading.md).
- In development, Vite proxies `/api` to the backend — see [local-dev.md](local-dev.md).

## Value objects

Entity models use branded value objects; API DTOs use scalars and are mapped in the `api` segment — see [value-objects.md](value-objects.md).

## Errors

Business errors are shown by the slice that made the request, next to the component or form that triggered it; system errors go to the single error modal (the `system-error-dialog` widget, mounted by `App`) — see [error-handling.md](error-handling.md).

## Styling

CSS Modules with a three-level variable system: a component uses only its own component variables, mapped to the theme in its theme file — see [styles.md](styles.md).
