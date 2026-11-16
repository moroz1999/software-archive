#!/bin/sh
# In a terminal (create-admin), Backspace erases a whole UTF-8 character, not a single byte of it.
[ -t 0 ] && stty iutf8 2>/dev/null
exec java -jar /app/app.jar "$@"
