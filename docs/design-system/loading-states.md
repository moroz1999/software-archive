# LoadingState

`LoadingState` — feedback for refreshing content that is already shown: pagination, filters, sorting, reload ([data-loading.md](../data-loading.md)).

- While `loading`, the content stays in place, dimmed and slightly blurred, marked `aria-busy` and closed to interaction.
- A spinner appears over the part of the content that is on screen, and only if the refresh lasts longer than 350 ms, so short refreshes never flash it. It follows scrolling and resizing, and fades out as soon as the fresh content arrives.
- The spinner lives outside the content (in a portal), so the blur never touches it.
