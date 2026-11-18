# ScreenshotGallery

- `ScreenshotGallery` — screenshots as a grid of 4:3 thumbnails, each a button named "Screenshot n of N". A thumbnail opens its picture whole in a full-screen `Dialog` titled by `title`, which goes round through the pictures by its Previous and Next buttons and the arrow keys and closes by Escape, by its cross or by the backdrop. The viewer reads the native picture size and follows its available area: a picture that fits is shown at the largest whole multiple of that size with 16 px left free at every edge; below 1× it shrinks just enough to fit.
- `ScreenshotGallerySkeleton` — the place of a gallery while its screenshots load: thumbnail bones in the same grid.
