# Design System

The project has its own design system. All its components live in `frontend/src/shared/ui/`, one folder per component with a public `index.ts`. Every component that gets built receives its own document in `docs/design-system/{component}.md`.

## Core Principles

1. **Design system only.** Interfaces are built only from `shared/ui` components. Third-party UI kits are not used.
2. **Component variables only.** A component's CSS uses only its own `--{component}-*` variables; they are mapped to semantic and base variables in the component's theme file — see [styles.md](styles.md#variable-hierarchy).
3. **Spacing.** Distances come from the base spacing scale (`--space-*`, reached through component variables); layout components take them as t-shirt sizes `none` … `xxl`.
4. **Themes.** Light and dark, chosen automatically by `prefers-color-scheme` — no manual switch, no theme classes. Component variables mapped to semantic ones make every component theme-aware.
5. **Semantic usage.** Every component is used for its intended purpose.
6. **Buttons.** Interactive controls in features and pages are `Button` — with `href` for links, `square` for icon-only, `color="transparent"` for low-emphasis triggers. Native `<button>`/`<a>` belong only inside atomic `shared/ui` components.
7. **Layout through components.** Spacing, alignment and positioning in business components are done only with layout components (`Stack`, `Inline`, `Grid`, `Inset`, `Panel`). `display: flex/grid`, `gap`, `padding` and layout margins in CSS are reserved for `shared/ui`. Negative margins are forbidden.
8. **Parent/child boundary.** A parent may style only the external contour of a child (`margin`, `min-/max-width`, `align-self`, `flex-grow`) via the child's `className`. Internal behaviour is varied through props or the component's CSS custom properties — never by styling from outside.
9. **No wrapper elements.** A component that only renders `children` adds no extra wrapper `div`.
10. **Loading states.** Every data-driven component has (see [data-loading.md](data-loading.md)):
    - first load — the skeleton matching its layout (no spinners, no empty containers);
    - pagination, filters, reload — a spinner over the dimmed content (`LoadingState`), controls locked; never a skeleton;
    - error — business errors inline with `ErrorState`, system errors through `ErrorDialog` (see [error-handling.md](error-handling.md)).
11. **Overlays.** Popovers, dropdowns and tooltips are rendered through the shared overlay primitive (portal + anchored positioning), never with `position: absolute` inside a `position: relative` host.

## Components

All components live in `frontend/src/shared/ui/`, one folder each with an `index.ts`; import each from its folder (`@/shared/ui/button`) — `shared/ui` has no barrel. Design tokens and themes are in `frontend/src/app/styles/`.

Built:

- Layout: [Stack](design-system/stack.md), [Inline](design-system/inline.md), [Inset](design-system/inset.md), [Grid, GridItem](design-system/grid.md), [CardGrid](design-system/card-grid.md), [Panel](design-system/panel.md), [PageLayout](design-system/page-layout.md)
- Content: [Text, Heading, Label, TextLink](design-system/typography.md), [Badge](design-system/badge.md), [Chip](design-system/chip.md), [ImagePlaceholder](design-system/image-placeholder.md), [Table](design-system/table.md)
- Controls: [Button](design-system/button.md), [Toggle](design-system/toggle.md)
- Navigation: [NavBar, NavBarLink](design-system/nav-bar.md), [NavChips, NavChip](design-system/nav-chips.md), [Pagination](design-system/pagination.md)
- Forms: [Input, Textarea](design-system/input.md), [Select](design-system/select.md), [Field](design-system/field.md), [Checkbox](design-system/checkbox.md), [MultiSelectFilter](design-system/multi-select-filter.md)
- Loading: [Skeleton](design-system/skeletons.md), [Spinner](design-system/spinner.md), [LoadingState](design-system/loading-states.md)
- Errors and overlays: [ErrorState](design-system/error-state.md), [Dialog, ErrorDialog](design-system/dialog.md), [ConfirmDialog](design-system/confirm-dialog.md)

Planned, built when a feature first needs them (each gets its document then):

### Layout
- `SidebarLayout` — sticky companion column beside the main content
- `Hero` — entity hero panel with slots: `HeroTitle`, `HeroBar`, `Facts`, `Location`, `Counters`, `MetaRow`, `ExtLinks`, `CreditsRow`
- `Callout` — recessed accent box with a left rule, optional full-area link
- `CollapsibleSection` — section that can be folded

### Typography and content
- `Preformatted` — fixed-width text with its own horizontal scroll
- `ItemData` — grouped key-value panel
- `ListItem` — pickable row with a second line and trailing actions
- `Tree` — collapsible tree of rows with row actions
- `Steps` — vertical list of process steps with a status marker
- `Chips` — a wrapping row of chips
- `Medal` — circular placement badge
- `LanguageFlag` — language marker
- `User` — user name with status badges
- `Breadcrumbs` — navigation trail
- `YoutubeEmbed` — embedded video

### Controls
- `ButtonControls` — group of buttons
- `CloseButton` — close control
- `Tabs` — tab navigation with content outlet
- `PopoverMenuItem` — item of a popover menu
- `IconPopover` — icon trigger with a popover of contextual actions

### Forms
- `Form` — form layout: fields, labels, fieldsets, sections, actions
- `CheckboxField`
- `InputRange`, `MinMaxRange`
- `TagsInput`, `TagsField`
- `MultilangField` — value per language
- `EntityAutocomplete`, `MultiEntityAutocomplete`
- `FileUpload`, `ImageUpload`
- `FilterBar`, `FilterPicker`, `SortSelect`

### Feedback and overlays
- `Tooltip` — floating tooltip
