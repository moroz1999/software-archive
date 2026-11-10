# Software View

The page of one software ([domain/software.md](../domain/software.md)) at `/software/{id}`: what visitors read before anyone edits. A card in the software list leads here; editing is one step further.

## Page

- The software's title is the page heading; "Edit" next to it leads to `/software/{id}/edit`.
- Under it: the release day in words ("20 September 1984") and the description, line by line. Unknown values are left out.
- Platform: the software's platform.
- Hardware: the software's base hardware, grouped by hardware group in the fixed order computers, storage, DOS, sound, controls, expansion; the section is left out without hardware.
- Releases: "Add release" next to the heading ([release-editing.md](release-editing.md)); "No releases known yet." when there are none. Otherwise:
  - a table of Title (with the description under it), Year, Version, Type, Hardware and "Edit", in the order they come; Title sorts from A, Year from the newest with unknown years last, a second click turns the order round;
  - "Type" shows the releases of one type, offered only when they are of more than one; an unknown type is not shown;
  - "Table / Cards" switches to cards with the same facts; a phone always shows cards, with nothing to switch.

## Hardware of a release

Each release shows its effective hardware ([domain/hardware.md](../domain/hardware.md#hardware-of-software-and-releases)): the backend merges it, so a group the release states replaces that group of the software and the rest comes from the software. The frontend shows it as given.

## Loading and errors

- The releases are a lazy block below: they load when they come into view, with a skeleton until then. It repeats their layout so nothing jumps: the bar with the View toggle at its end, the same table header over rows as high as real ones, the columns fixed in width; cards on a phone.
- The page preloads the software's details ([data-loading.md](../data-loading.md)): within 300 ms it appears filled, otherwise with a skeleton of the heading and the details.
- Unknown software, and an id in the URL that cannot be one, show the not found page ([error-handling.md](../error-handling.md)); a malformed id is never sent to the backend.
- Any other business error is shown inline with Retry; system errors go to the error dialog ([error-handling.md](../error-handling.md)).

## REST

| Call | Answer | Errors |
|---|---|---|
| `GET /api/software/{id}` | the same call as the editing ([software-editing.md](software-editing.md#rest)) | |
| `GET /api/software/{id}/releases` | `[{id, title, version, type, releaseDate, description, hardware: [{code, name, group}]}]` by release day, unknown days last; `title` falls back to the software's title | `404 software.not-found`; `400` for a malformed id |

`type` is one of `original`, `rerelease`, `adaptation`, `localization`, `bugfix`, `mod`, `crack`, `mia`, `corrupted`, `compilation`, `incomplete`, `demoversion`, `unknown`; `group` is one of `computers`, `storage`, `dos`, `sound`, `controls`, `expansion`.
