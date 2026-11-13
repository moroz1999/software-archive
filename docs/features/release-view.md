# Release View

The page of one release ([domain/software.md](../domain/software.md#releases)) at `/software/{id}/releases/{releaseId}`. Release titles on the software page lead here.

## Page

- The release title is the page heading. "Edit" leads to its editing page when the visitor may change the release.
- The software is named first and links back to its page.
- Release date (a year as it is, a day in words), version and description are left out when unknown. Description lines stay separate.
- Type is always shown. Hardware is the effective hardware returned for the release, grouped in the archive's fixed hardware-group order.
- When the release has an archive, its original file name and size are shown with a download action.
- The release's screenshots appear in their gallery below the facts and are left out when there are none ([screenshots.md](screenshots.md#pages)).

## Loading and errors

- The page preloads the software and its releases together ([data-loading.md](../data-loading.md)); within 300 ms it appears filled, otherwise with a skeleton matching the title and facts.
- Screenshots are a lazy block with thumbnail skeletons until they come into view.
- Unknown software or release, and an id in the URL that cannot be one, show the not found page. A malformed id is never sent to the backend.
- Business and system read errors go to the error dialog with Retry ([error-handling.md](../error-handling.md)).

## REST

The page uses the software details and release list calls documented by [software-view.md](software-view.md#rest), so it reads the same effective hardware, display title, archive metadata and change permission as the software page. Its screenshot block uses the release screenshot call documented by [screenshots.md](screenshots.md#rest). Archive downloads are documented by [release-files.md](release-files.md#rest).
