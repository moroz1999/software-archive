# ConfirmDialog

`ConfirmDialog` — asks before a destructive action: an `alertdialog` with the `title`, the question as children, Cancel and a danger button with `confirmLabel`, which names the action ("Delete for good", never "OK").

- `onConfirm` runs the action; `onCancel` is called by Cancel and Escape.
- `busy` while the action runs: both buttons are locked, the confirm button is busy, Escape does nothing.
- `error` — a business error of the action, shown inside the dialog; system errors go to the error dialog.
