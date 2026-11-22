/** The day and the time of a moment of a run in the visitor's time zone: `14.11.2026 12:05`. */
export function runTimeText(iso: string): string {
    const date = new Date(iso);
    const two = (value: number) => String(value).padStart(2, '0');
    return `${two(date.getDate())}.${two(date.getMonth() + 1)}.${String(date.getFullYear())} ${two(date.getHours())}:${two(date.getMinutes())}`;
}

/** The time of a step of a run in the visitor's time zone, to the second: `12:05:42`. */
export function logTimeText(iso: string): string {
    const date = new Date(iso);
    const two = (value: number) => String(value).padStart(2, '0');
    return `${two(date.getHours())}:${two(date.getMinutes())}:${two(date.getSeconds())}`;
}
