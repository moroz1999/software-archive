# Software View

The page of one software ([domain/software.md](../domain/software.md)) at `/software/{id}`: what visitors read before anyone edits. A card in the software list leads here; editing is one step further.

## Page

- The software's title is the page heading; "Edit" next to it leads to `/software/{id}/edit`.
- Under it: the release day in words ("20 September 1984") and the description, line by line. Unknown values are left out.

## Loading and errors

- The page preloads the software's details ([data-loading.md](../data-loading.md)): within 300 ms it appears filled, otherwise with a skeleton of the heading and the details.
- Unknown software, and an id in the URL that cannot be one, show the not found page ([error-handling.md](../error-handling.md)); a malformed id is never sent to the backend.
- Any other business error is shown inline with Retry; system errors go to the error dialog ([error-handling.md](../error-handling.md)).

## REST

`GET /api/software/{id}` — the same call as the editing ([software-editing.md](software-editing.md#rest)).
