# Platform Switch

The global platform switch ([domain/hardware.md](../domain/hardware.md#platform-switch)) sits on the right of the main menu ([main-menu.md](main-menu.md)): "All platforms" or one of the archive's platforms, filled from the backend.

## Selection

- The choice is kept in the browser (`localStorage`, key `archive.platform`) and survives reloads; an unreadable or blocked storage means "All platforms".
- `PlatformSelectionProvider` in `app` hands the choice to every list; route loaders run outside React, so they read the stored choice directly to preload the same data the page will show.
- Every list that follows the switch keeps the platform in its query key: switching shows the spinner over the old data until the new arrives ([data-loading.md](../data-loading.md)).
- Followed by: the software list, the home page, the author and group catalogs ([software-list.md](software-list.md), [home.md](home.md), [author-catalog.md](author-catalog.md), [group-catalog.md](group-catalog.md)). The new software form starts with the chosen platform.

## Platform inside software

Software holds its platform as part of its own aggregate (`entities/software`), not as the platforms entity: the form gets the list of choices from its feature, and the page shows the platform the details carry.

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/platforms` | `[{code, name}]` by name | |

The lists take `platform={code}`; without it they cover every platform. An unknown code gives an empty list, a malformed one `400 platform.code-invalid`. Authors and groups are filtered through credits on software of the platform.
