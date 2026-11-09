# Frontend

React 19, TypeScript, Vite, React Router, TanStack Query, CSS Modules.

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
- Imports across layers and slices start at the root of the sources, `@/` (`@/entities/software`); relative paths stay inside a slice or a module and go up one level at most (`../model/…`).
- `shared/ui` and `shared/lib` have no barrel: a component or a module is imported from its own folder or file (`@/shared/ui/button`, `@/shared/lib/context`), so a module pulls in only what it uses — into the bundle and into every test. A folder module keeps its `index.ts` (`shared/ui/button`, `shared/lib/test`). `shared/api` is one module with one public API, `@/shared/api`, as Steiger requires for segments other than `ui` and `lib`; so is `shared/config` (`HOME_PATH`).
- A page slice never exports its component statically: its `index.ts` exports the route loader and a `load…Page` function that imports the component dynamically (`lazy: {Component: loadSoftwarePage}` in the router). A static export would pull the page into the main bundle.
- `package.json` declares `"sideEffects": ["**/*.css"]`: JS modules have no side effects. Public APIs re-export UI next to data calls, and a component importing its CSS module would otherwise count as a side effect and stay in the main bundle with everything it renders. Module-level code with side effects (subscriptions, global registrations) is therefore not allowed outside `app/`.
- Inside a slice, code is split by segments: `ui/`, `model/`, `api/`, `lib/`, `config/`.
- `app/` and `shared/` have no slices, only segments.
- Domain knowledge never appears in `shared/`.
- The rules are enforced by Steiger: `docker compose exec frontend npm run lint:fsd`. `fsd/insignificant-slice` is off: a young slice used by one page is not merged back into it. `fsd/repetitive-naming` is off: slices are named `{entity}-{action}` (`software-list`, `software-edit`), so they share the entity prefix. Entity slices are named in the plural (`authors`; `software` is its own plural), as `fsd/inconsistent-naming` requires.

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
| `@/` across layers and slices; no `../../`; no `shared/ui`, `shared/lib` barrels | `no-restricted-imports` |
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
- Server state is held by TanStack Query: one `QueryClient` per page load (`createQueryClient()` from `shared/api`), shared by the route loaders and the blocks.
- When and how blocks load — route preload with a 300 ms window, lazy loading by viewport, skeletons on first load, spinners for pagination and filters — is defined in [data-loading.md](data-loading.md).
- In development, Vite proxies `/api` to the backend — see [local-dev.md](local-dev.md).

## State: providers and context

Components with markup do not hold business state. It lives in **providers**, and markup reads it from their context.

- Business state is what the archive is about: server data (queries), user actions on it (mutations), the values and errors of a form being filled in. A provider in the slice's `model` segment holds it — `useQuery`/`useMutation`, `useState` for form values, cache invalidation after a change — and hands it down through a context.
- A provider renders no markup of its own: only its context provider around `children`, or another provider it configures (an edit feature's provider wraps the entity's form provider, giving it the initial values, the submit and the business error).
- The context and its hook are declared with `createRequiredContext(name)` (`shared/lib`) in their own `*-context.ts` file of the `model` segment, next to the provider: the hook fails loudly outside its provider, and the provider file exports only the component (fast refresh).
- Components in `ui` read the hook (`useSoftwareDetails()`, `useSoftwareForm()`, …) and render.
- A slice exports its provider and its markup separately; whoever uses them — a widget or a page — puts the markup inside the provider. No wrapper component exists only to glue the two. A component wraps its own provider only when that markup is the slice's own control and nothing else would compose it (`DeleteSoftwareButton`: the button and its confirmation).
- UI state stays in the component it belongs to: whether a dialog is open, a ripple, a hover. It is not business state and needs no provider. Atoms in `shared/ui` and hooks in `shared/lib` keep their own state.

```
entities/software/
  model/software-form-context.ts      state type + [Context, useSoftwareForm]
  model/SoftwareFormProvider.tsx      values, format errors, refusal → context
  ui/SoftwareForm.tsx                 fields reading useSoftwareForm()
features/software-edit/
  model/EditSoftwareProvider.tsx      useMutation, invalidation → <SoftwareFormProvider …>
widgets/software-editor/
  ui/SoftwareEditor.tsx               <EditSoftwareProvider …><SoftwareForm submitLabel="Save"/></EditSoftwareProvider>
```

## Value objects

Entity models use branded value objects; API DTOs use scalars and are mapped in the `api` segment — see [value-objects.md](value-objects.md).

## Errors

Business errors are shown by the slice that made the request, next to the component or form that triggered it; system errors go to the single error modal (the `system-error-dialog` widget, mounted by `App`) — see [error-handling.md](error-handling.md).

## Styling

CSS Modules with a three-level variable system: a component uses only its own component variables, mapped to the theme in its theme file — see [styles.md](styles.md).
