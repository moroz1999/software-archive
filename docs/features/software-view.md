# Software View

The page of one software ([domain/software.md](../domain/software.md)) at `/software/{id}`: what visitors read before anyone edits. A card in the software list leads here; editing is one step further.

## Page

- The software's title is the page heading; the menu right after its text ([PopoverMenu](../design-system/popover-menu.md)) holds "Edit", leading to `/software/{id}/edit`, for whoever may change the software. Over the heading, the breadcrumbs: Software, the branch of its first category from the top, each step leading to the catalog of that category, then the software itself ([domain/software.md](../domain/software.md)). The browser tab reads the title and the platform: "Elite (Vector) — Ретротека".
- Under it: the release date, a year ("1987") or a day ("20.09.1984"), and the description, line by line; under the description, its own section "Instructions", line by line. Unknown values are left out.
- What the catalog is filtered by leads to the catalog filtered by it alone ([software-list.md](software-list.md#address)): the release date to the catalog of its year, each chip of a category branch to the catalog of that category.
- Platform: the software's platform.
- Preservation status: left out while unknown.
- Categories: each category of the software with its whole branch from the top of the tree, one line of chips joined by "›" per category (`Games › Action › Shooters`), in the order of the tree; the section is left out without categories.
- Authors: every person, group or exact label with the role chips beside it, developers and publishers among them, an unknown role not shown; a person or a group leads to their page, a label always to its own page ([label-view.md](label-view.md)). The section is left out without credits.
- Hardware: the software's base hardware as technical chips, grouped by hardware group in the fixed order computers, storage, DOS, sound, controls, expansion; the section is left out without hardware.
- Screenshots: a gallery of the software's own screenshots, left out without any; whoever may change the software moves them on their thumbnails ([screenshots.md](screenshots.md#pages)).
- Maps, Covers, Adverts: after the screenshots, a gallery each, the same way; each left out without any.
- Program on other sites: a link to the page of each archive or site the software was imported or taken from, named by the archive (`vektor-06c.ru`, `zxart.ee`); left out for software typed in by hand ([vektor-import.md](vektor-import.md)).
- Releases: "Add release" next to the heading ([release-editing.md](release-editing.md)); "No releases known yet." when there are none. Otherwise:
  - a table of Title (leading to the release page, with the first screenshot beside it and the description under it), Release date, Version, Type, Hardware, Credits (the names credited on the release with their role chips, leading as the authors of the software do), Archive ("Download" with the size of the archive, empty without one) and "Edit", in the order they come; Title sorts from A, Release date from the newest with unknown dates last, a second click turns the order round;
  - "Type" shows the releases of one type, offered only when they are of more than one; an unknown type is not shown;
  - "Table / Cards" switches to cards with the same facts, the credits, "Download" and the screenshots of each release; a phone always shows cards, with nothing to switch.
- Last: who added the software and on which day ("Added by Zork on 4 March 2026"), only the day once their account is gone ([domain/users.md](../domain/users.md#who-added-an-entry)).

## Hardware of a release

Each release shows its effective hardware ([domain/hardware.md](../domain/hardware.md#hardware-of-software-and-releases)): the backend merges it, so a group the release states replaces that group of the software and the rest comes from the software. The frontend shows it as given.

## Loading and errors

- The screenshots, maps, covers and adverts are lazy blocks too, with thumbnail skeletons until they come.
- The releases are a lazy block below: they load when they come into view, with a skeleton until then. It repeats their layout so nothing jumps: the bar with the View toggle at its end and the same table header over rows as high as real ones; cards on a phone. The table scrolls sideways when its content needs more room.
- The page preloads the software's details ([data-loading.md](../data-loading.md)): within 300 ms it appears filled, otherwise with a skeleton of the heading and the details.
- Unknown software, and an id in the URL that cannot be one, show the not found page ([error-handling.md](../error-handling.md)); a malformed id is never sent to the backend.
- Any other business error is shown inline with Retry; system errors go to the error dialog ([error-handling.md](../error-handling.md)).

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/software/{id}` | the same call as the editing ([software-editing.md](software-editing.md#rest)) | |
| `GET /api/software/{id}/releases` | `[{id, title, version, type, releaseDate, description, hardware: [{code, name, group, groupName}], screenshots: [{id, url}], archive: {fileName, size, url}, credits}]` (`archive` is `null` without one; `credits` as in the software details) by release date — a year before the days of that year —, unknown dates last; `title` falls back to the software's title; `added: {by, at}` as for the software | `404 software.not-found`; `400` for a malformed id |

`type` is one of `original`, `rerelease`, `adaptation`, `localization`, `bugfix`, `mod`, `crack`, `mia`, `corrupted`, `compilation`, `incomplete`, `demoversion`, `unknown`; `group` is one of `computers`, `storage`, `dos`, `sound`, `controls`, `expansion`.
