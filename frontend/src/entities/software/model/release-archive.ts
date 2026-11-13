/** The one downloadable file of a release. */
export interface ReleaseArchive {
    readonly fileName: string;
    readonly size: number;
    readonly url: string;
}

export const RELEASE_ARCHIVE_MAX_BYTES = 100 * 1024 * 1024;

export function releaseArchiveProblem(file: File): 'too-large' | undefined {
    return file.size > RELEASE_ARCHIVE_MAX_BYTES ? 'too-large' : undefined;
}

/** A compact binary size for visitor-facing file metadata. */
export function fileSizeText(bytes: number): string {
    if (bytes < 1024) {
        return `${String(bytes)} B`;
    }
    const units = ['KB', 'MB', 'GB'] as const;
    let value = bytes / 1024;
    let unit: string = units[0];
    for (const next of units.slice(1)) {
        if (value < 1024) {
            break;
        }
        value /= 1024;
        unit = next;
    }
    return `${new Intl.NumberFormat('en', {maximumFractionDigits: 1}).format(value)} ${unit}`;
}
