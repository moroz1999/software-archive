# MultiEntityAutocomplete

`MultiEntityAutocomplete` chooses several entities from remote search results.

- Typing is debounced before loading; a blank query loads nothing.
- Results open in an anchored overlay and support pointer, Arrow Up, Arrow Down, Enter and Escape.
- Chosen entities stay in selection order as removable chips and disappear from later results.
- Controlled: the caller owns the chosen values and the remote loader; an entity is identified by both its type and id.
- Read failures use the application's error dialog; a field refusal is shown by the surrounding field.
