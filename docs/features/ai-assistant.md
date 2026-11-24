# AI assistant

The rules: [domain/ai-assistant.md](../domain/ai-assistant.md).

## Tools

The tools are adapters over the use cases, in `assistant/web`, beside the controllers: each calls the query handlers of the domains as the editor (`ToolContext`: the actor and the language of the content). They know nothing of the chat, so another transport (an MCP server) can offer the same registry.

- `Tool` has a name, a description for the model, a hand-written JSON Schema of its arguments and the record they are read into; `ReadTool` reads and is called freely.
- `ToolRegistry` holds every tool bean by name; two of one name stop the application. `ToolCalls` runs a call as the model writes it: name and JSON.
- `ToolArguments` checks the JSON against the schema (`networknt` JSON Schema validator, formats asserted, paths named as `$.filter.year`) and reads it into the record. Every schema is closed (`additionalProperties: false`) at every depth; a field that is not required is nullable, and `null` reads as not given, so a model that fills every field leaves it out with `null`. A call that does not fit is `ToolArgumentsException`; an unknown name is `UnknownToolException`; a business exception of the use case passes through. Each is told to the model.
- Lists answer `ToolPage` (`total`, `page` counted from 1, `size`, `items`); a page is 50 entries unless asked, at most 100 (`ToolPages`).
- The tools that read a list of a page (`search`, `search_software`, `list_latest_software`, `list_authors`, `list_groups`) take the same filters as that page, so the page context the editor sends names the call that reads what they see.
- An entry is read as its page shows it: `get_software` (with its screenshots and releases), `get_release` (with its screenshots and the file tree of its archive, if it has one), `get_author`, `get_group`, `get_label` (each with the software and releases credited on it). They answer the read models of their domains.
- `find_names` is the credit picker of the software form: the only search that finds labels of nobody.
- `find_places` runs `FindPlacesHandler` of the geo domain: `JdbcGeoReadRepository.findPlaces` matches every name of a country or a city by the key the duplicates are compared by (`JdbcGeoDuplicates.key`).
- `ToolCatalogIT` checks every tool: its description, its schema closed, and its schema naming exactly the components of its arguments record (and of the records in its lists), requiring those that are not `@Nullable`.

### Write tools

- `WriteTool` changes the archive and is only put into a plan: `ToolCalls.read` refuses it, and `ToolCalls.state`/`write` refuse a read tool. It names its `ChangeKind` and the type of the entry it changes, and has two calls:
  - `state`: the entry as it is now, for the plan to show; none for a create. An edit shows the entry in the names of its own arguments (`SoftwareState`, `ReleaseState`, `AuthorState`, `GroupState`, `LabelState`), so the plan sets what is beside what becomes; a delete, a merge or a conversion shows the entry as its `get_*` tool reads it, with what goes with it. An id of nothing is the use case's not-found.
  - `write`: makes the change and answers `Written`, the id it produced (the entry added, changed, merged into or made; the code of hardware) for a later change of the plan.
- A tool goes through the module's own form: it builds the request DTO of its controller and converts it with the module's web mapper, so a write by the assistant is checked exactly as one by the form. The commands without a form body (deleting, attaching, merging, making an owner of a label) are built directly, as their controllers build them.
- An edit is the form sent whole: `*Forms` reads the entry as it is and `Changes` lays the arguments given over it (a field left out stays, an empty text clears it, a list given replaces it). `set_*_credit`, `remove_*_credit`, `set_group_member` and `remove_group_member` change one item of that list; taking away one that is not there is refused.
- Places and hardware go through the management's own requests (`PlaceRequestDto`, `HardwareRequestDto`) and mappers; `NamesArguments` gives the Russian name and the English one. A geo merge or move shows the city or the country as the management reads it.
- `update_label` keeps the owner the label has; `detach_label` is the label form sent with no owner. Uploads (archives, screenshots) are not written yet.
- The schemas take their enumerations from the domain: credit and member roles, reference types, release types, preservation statuses. Roles outside them are refused before the use case runs.
- `*WriteToolsIT`, one per area, run each tool through `ToolCalls` against the database and read the effect back with the read tools.

## Release search

`search_releases` runs `SearchReleasesHandler` of the search domain over `JdbcReleaseSearch`: plain SQL over the releases, their software, credits and file trees, apart from the search index. A release's hardware is its own and its software's in the groups it does not state. The credits of each release found are read by `SoftwareReadRepository.findReleaseCredits`.

## Skills

A skill is a Markdown file in `backend/src/main/resources/assistant/skills/`: `<name>.md`, opened by a frontmatter of `name` (the file's name) and `description` (when to load it); the rest is the instructions. `ClasspathSkillReadRepository` reads them; a file that breaks this is refused, and `ClasspathSkillReadRepositoryTest` reads the archive's skills, so a broken one fails the build. `load_skill` answers one; an unknown name is `SkillNotFoundException`, naming the skills there are.

## Other sites

The `remote` module reads what other sites serve, for the tools only: `FetchPageHandler`, `ViewImageHandler` and `DownloadFileHandler` take a `RemoteUrl` (absolute, `http`/`https`, with a host) and go through `RemoteResources`, a read port that `GuardedRemoteResources` implements with OkHttp:
- Redirects are followed by hand, at most five; before each request every address the host resolves to (an IP written as the host too) must pass `AddressPolicy`, and the call is pinned to those addresses through its own `Dns`, so a second DNS answer cannot lead it elsewhere. No proxy, no cookies, a neutral User-Agent; 10 s to connect, 60 s a call.
- `PublicAddressPolicy` refuses loopback, any-local, link-local (169.254/16, fe80::/10), private (10/8, 172.16/12, 192.168/16), unique-local (fc00::/7), multicast, 0/8, 100.64/10, 192.0.0/24, 198.18/15 and 240/4, and an IPv4 address carried in IPv6 (mapped, compatible, NAT64) as itself. The integration tests replace it with one letting the IPv4 loopback through (`RemoteTestConfiguration`), where their `TestSite` runs.
- A page is read up to its limit and decoded from the charset of the header, else of an HTML `<meta>`, else UTF-8; a character the cut falls inside is left out. A response that is not text, JSON, XML or JavaScript has no body.
- A picture must answer 200 and is judged by its first bytes.
- A download answers 200, is streamed into `TEMPORARY_FOLDER` and refused as soon as it grows past its limit; `DownloadedFile` deletes it once closed. Its name is that of `Content-Disposition` (`filename*` first), else the last segment of the address, never a path.
- A refusal is `RemoteRequestRefusedException`, told to the model like any business exception.

`fetch_url` and `view_image` read; `view_image` reads a screenshot of the archive from its stored file too. `add_screenshots` downloads every picture before it adds any and hands them to `AddScreenshotsHandler` as an upload would; `set_release_archive` sends the release whole with the archive (`ReleaseWebMapper.toUpdateWithArchive`); `delete_screenshot` shows the screenshot and deletes it.
