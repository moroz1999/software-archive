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
- `ToolCatalogIT` checks every tool: its description, its schema closed, and its schema naming exactly the components of its arguments record, requiring those that are not `@Nullable`.

## Release search

`search_releases` runs `SearchReleasesHandler` of the search domain over `JdbcReleaseSearch`: plain SQL over the releases, their software, credits and file trees, apart from the search index. A release's hardware is its own and its software's in the groups it does not state. The credits of each release found are read by `SoftwareReadRepository.findReleaseCredits`.

## Skills

A skill is a Markdown file in `backend/src/main/resources/assistant/skills/`: `<name>.md`, opened by a frontmatter of `name` (the file's name) and `description` (when to load it); the rest is the instructions. `ClasspathSkillReadRepository` reads them; a file that breaks this is refused, and `ClasspathSkillReadRepositoryTest` reads the archive's skills, so a broken one fails the build. `load_skill` answers one; an unknown name is `SkillNotFoundException`, naming the skills there are.
