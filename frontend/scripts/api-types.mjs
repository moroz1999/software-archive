// Generates the TypeScript types of the REST API from the backend's OpenAPI document (docs/frontend.md#data-loading).
import fs from 'node:fs';
import openapiTS, {astToString} from 'openapi-typescript';
import ts from 'typescript';

const source = new URL('../src/shared/api/openapi.json', import.meta.url);
const target = new URL('../src/shared/api/schema.ts', import.meta.url);

const ast = await openapiTS(source, {
    immutable: true,
    // Every fixed set of values of the contract also as a list, for the choices a form offers.
    enumValues: true,
    // Free JSON (the arguments of an assistant's tool call) is an object of unknown values.
    emptyObjectsUnknown: true,
    // A file of a multipart form is a Blob in the browser, not a string.
    transform(schema) {
        return schema.format === 'binary' ? ts.factory.createTypeReferenceNode('Blob') : undefined;
    },
});
fs.writeFileSync(target, astToString(ast));
