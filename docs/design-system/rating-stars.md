# RatingStars

`RatingStars` — five stars filled as far as a rating goes, a part of a star for a share of one.

- `value` — the stars filled, 0–5; `label` names the rating for assistive technology (`role="img"`).
- With `onRate(stars)` every star is a button giving that many (`role="group"`, the buttons named by `starLabel(stars)`); pointing at or focusing a star fills the stars it would give. `chosen` marks the button of the reader's stars as pressed for assistive technology, with no mark of its own on the stars; `disabled` locks them.
- `title` — a tooltip over the stars, as the number of votes.
- `size`: `md` (default) or `sm`, for cards and lists.
