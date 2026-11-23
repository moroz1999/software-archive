# Release View

The page of one release ([domain/software.md](../domain/software.md#releases)) at `/software/{id}/releases/{releaseId}`. Release titles on the software page lead here.

## Page

- The release title is the page heading. The menu right after its text ([PopoverMenu](../design-system/popover-menu.md)) has "Edit", leading to its editing page, when the visitor may change the release. Over the heading, the breadcrumbs of its software ([software-view.md](software-view.md#page)), the software leading to its page, then the release. The browser tab reads the release title and the platform of its software: "Elite AY (Vector) — Ретротека".
- The software is named first and links back to its page.
- Release date (a year, or a day as dd.mm.yyyy), version and description are left out when unknown. Description lines stay separate.
- Type is always shown. Hardware is the effective hardware returned for the release, grouped in the archive's fixed hardware-group order.
- Credits: every person, group or exact label credited on the release, as the authors of software ([software-view.md](software-view.md#page)); left out without credits.
- When the release has an archive, its original file name and size are shown with a download action.
- The parsed file structure follows the download as a collapsible tree. The root starts open, nested levels are indented, and executable files are marked. A file not parsed yet shows "Waiting for the structure to be parsed" in place of the tree. An unreadable file shows "The file could not be read" instead. Whoever may change the release sees "Parse again" under it: the file is parsed in the request, the button stays busy meanwhile, then the tree is loaded anew; a refusal is shown beside the button, other failures go to the error dialog.
- The release's screenshots appear in their gallery below the facts and are left out when there are none; whoever may change the release moves them on their thumbnails ([screenshots.md](screenshots.md#pages)).
- Last: who added the release and on which day ("Added by Zork on 4 March 2026"), only the day once their account is gone ([domain/users.md](../domain/users.md#who-added-an-entry)).

## Loading and errors

- The page preloads the software and its releases together and, when the release has a file, its parsed structure ([data-loading.md](../data-loading.md)); within 300 ms it appears filled, otherwise with a skeleton matching the title and facts.
- Screenshots are a lazy block with thumbnail skeletons until they come into view.
- Unknown software or release, and an id in the URL that cannot be one, show the not found page. A malformed id is never sent to the backend.
- Business and system read errors go to the error dialog with Retry ([error-handling.md](../error-handling.md)).

## REST

The page uses the software details and release list calls documented by [software-view.md](software-view.md#rest), so it reads the same effective hardware, display title, archive metadata and change permission as the software page. Its screenshot block uses the release screenshot call documented by [screenshots.md](screenshots.md#rest). Archive downloads and the parsed structure call are documented by [release-files.md](release-files.md#rest).
