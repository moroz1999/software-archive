/**
 * The `bodySerializer` of a multipart request: each file, or each of a list of files, is a part under its name; any other value is a JSON part, as
 * the backend reads a `@RequestPart` record. A value not given is left out.
 */
export function multipartForm(body: Readonly<Record<string, unknown>> | undefined): FormData {
    const form = new FormData();
    Object.entries(body ?? {}).forEach(([name, value]) => {
        if (value instanceof Blob) {
            form.append(name, value);
        } else if (Array.isArray(value) && value.every((part) => part instanceof Blob)) {
            value.forEach((part: Blob) => {
                form.append(name, part);
            });
        } else if (value !== undefined) {
            form.append(name, new Blob([JSON.stringify(value)], {type: 'application/json'}));
        }
    });
    return form;
}
