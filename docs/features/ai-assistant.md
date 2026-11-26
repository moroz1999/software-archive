# AI assistant

The rules: [domain/ai-assistant.md](../domain/ai-assistant.md).

## Tools

The tools are adapters over the use cases, in `assistant/web`, beside the controllers: each calls the query handlers of the domains as the editor (`ToolContext`: the actor and the language of the content). They know nothing of the chat, so another transport (an MCP server) can offer the same registry.

- `Tool` has a name, a description for the model, a hand-written JSON Schema of its arguments and the record they are read into; `ReadTool` reads and is called freely.
- `ToolRegistry` holds every tool bean by name; two of one name stop the application. `ToolCalls` runs a call as the model writes it: name and JSON.
- `ToolArguments` checks the JSON against the schema (`networknt` JSON Schema validator, formats asserted, paths named as `$.filter.year`) and reads it into the record. Every schema is closed (`additionalProperties: false`) at every depth; a field that is not required is nullable, and `null` reads as not given, so a model that fills every field leaves it out with `null`. A call that does not fit is `ToolArgumentsException`; an unknown name is `UnknownToolException`; a business exception of the use case passes through. Each is told to the model.
- Lists answer `ToolPage` (`total`, `page` counted from 1, `size`, `items`); a page is as many entries as the page of that list on the site unless asked — the page size of its query (`ListAuthorsQuery.PAGE_SIZE` and the like), which its controller takes too — 50 for a list without a page, at most 100 (`ToolPages`).
- The tools that read a list of a page (`search`, `search_software`, `list_latest_software`, `list_authors`, `list_groups`) take the same filters as that page, so the page context the editor sends names the call that reads what they see.
- An entry is read as its page shows it: `get_software` (with its screenshots and releases), `get_release` (with its screenshots and the file tree of its archive, if it has one), `get_author`, `get_group`, `get_label` (each with the software and releases credited on it). They answer the read models of their domains.
- `find_names` is the credit picker of the software form: the only search that finds labels of nobody.
- `find_places` runs `FindPlacesHandler` of the geo domain: `JdbcGeoReadRepository.findPlaces` matches every name of a country or a city by the key the duplicates are compared by (`JdbcGeoDuplicates.key`).
- `describe_write_tool` answers a write tool by its name: what it does, its kind, the type of entry it changes and its schema, which the model reads before it plans the tool. It takes the list of write tools, not the registry, which holds it.
- `ToolCatalogIT` checks every tool: its description, its schema closed, and its schema naming exactly the components of its arguments record (and of the records in its lists), requiring those that are not `@Nullable`.

### Write tools

- `WriteTool` changes the archive and is only put into a plan: `ToolCalls.read` refuses it, and `ToolCalls.state`/`write` refuse a read tool. It names its `ChangeKind` and the type of the entry it changes, and has two calls:
  - `state`: the entry as it is now, for the plan to show; none for a create. An edit shows the entry in the names of its own arguments (`SoftwareState`, `ReleaseState`, `AuthorState`, `GroupState`, `LabelState`), so the plan sets what is beside what becomes; a delete, a merge or a conversion shows the entry as its `get_*` tool reads it, with what goes with it. An id of nothing is the use case's not-found.
  - `write`: makes the change and answers `Written`, the id it produced (the entry added, changed, merged into or made; the code of hardware) for a later change of the plan.
- A tool goes through the module's own form: it builds the request DTO of its controller and converts it with the module's web mapper, so a write by the assistant is checked exactly as one by the form. The commands without a form body (deleting, attaching, merging, making an owner of a label) are built directly, as their controllers build them.
- An edit is the form sent whole: `*Forms` reads the entry as it is and `Changes` lays the arguments given over it (a field left out stays, an empty text clears it, a list given replaces it). `set_*_credit`, `remove_*_credit`, `set_group_member` and `remove_group_member` change one item of that list; taking away one that is not there is refused.
- Places and hardware go through the management's own requests (`PlaceRequestDto`, `HardwareRequestDto`) and mappers; `NamesArguments` gives the Russian name and the English one. A geo merge or move shows the city or the country as the management reads it.
- `update_label` keeps the owner the label has; `detach_label` is the label form sent with no owner. Files come from other sites ([Other sites](#other-sites)).
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

## Chat

### Access and storage

- `UserGroup.AI_ASSISTANT` (`ai-assistant`) is given on purpose like the administrator and the moderator (`GrantedGroups`); `Actor.mayUseAssistant()` holds for a signed-in member of it. Every use case of the chat checks it first (`ChatAccess`), then that the conversation is the actor's: another's does not exist for them (`ConversationNotFoundException`, `PlanNotFoundException`).
- `V39__assistant_chat.sql` keeps `assistant_conversation` (owner, title, last activity, `last_seq`), `assistant_message` (`seq`, role, kind, text, JSON payload), `assistant_plan` (title, status, its changes as one JSON array) and `assistant_run` (type, plan, status, the stop asked for, steps, tokens, error). Deleting a conversation, or its owner's account, deletes what it holds.
- The domain is `assistant/domain/chat`: `Conversation` (titled after its first message, cut to 80 characters on one line), `ConversationMessage` (never changed; its repository gives it the next `seq` by `UPDATE … SET last_seq = last_seq + 1 … RETURNING` on the conversation's row), `Run` and `Plan` with `PlanAction`. The `Jdbc*Repository` classes read a row to be changed `FOR UPDATE`, so two starters cannot claim one run.
- Message kinds and what their payload holds: `text` (of the editor: `page`), `toolCall` (`callId`, `tool`, `arguments`, `result`), `plan` (`callId`, `planId`, `arguments`), `planProgress` (`planId`, `position`, `status`), `error`, `stopped`. The read side names kinds and statuses in camel case (`toolCall`, `waitingApproval`).

### The model

`LanguageModel` is a port of the read side (`ModelRequest` of `ModelMessage`s and `ModelTool`s, `ModelReply`); `AskModelHandler` asks it in no transaction of its own, so a run holds no database connection while the model thinks. `OpenAiLanguageModel` implements it over the OpenAI Responses API with function tools and `store: false`, through OkHttp: a picture goes as an `input_image` data URL; a refused request, a response `failed` and a key that is not set (`assistant.openai.api-key`, from `OPENAI_API_KEY`) are `ModelFailedException`. The model is `assistant.openai.model`. Every exchange is logged under `assistant.exchanges`: of a request only the input items not logged before in its run (for its first request, those after the model's last turn), pictures as `<type, size>`; then the raw response without its echo of the tools, or the failure.

### A run

`ChatService` is what a transport calls; the REST controller now, a socket or an MCP server later. `send` stores the message (`SendMessageHandler`: one run at a time per conversation, `AssistantBusyException`, unless the active one is past `Run.DEAD_AFTER`, 15 minutes, and is failed as dead; a proposed plan is superseded) and hands the run to `RunLauncher`, which `BackgroundRunLauncher` puts on `applicationTaskExecutor`.

`AgentRunner` claims the run and loops: before each step it checks the stop asked for (a `stopped` message) and its limits, 50 steps and 5 minutes (an `error`); `ConversationHistory` turns the stored messages into the model's — the system prompt (`SystemPrompt`, the rules and the list of skills) first, the page an editor wrote from appended to their message, a tool call as the call and its result, a plan as the call of `propose_plan`, plan progress and a stop as system notes, errors left out. The model is offered every read tool and `propose_plan`. Each reply is stored before it is acted on: prose as `text`, each call as `toolCall` with its result; a call that fails is told as `Error: …` and the run goes on. A picture `view_image` showed follows the result of its call in the requests of this run only; the result stored says only its type and size. A reply without calls ends the run `done`.

### Plans

`PlanProposer` checks `propose_plan` (`title`, `actions` of `tool` and `arguments`, nothing else): each change must name a write tool; `PlanReferences` finds its `{"$ref": N}` and refuses one to itself or a later change; a change without references reads its state (`ToolCalls.state`), one with them has its arguments checked against the schema with a stand-in id (`ToolCalls.check`). A plan that does not hold goes back to the model as `Error: Change N (tool): …`; one that holds is stored (`ProposePlanHandler`), told as a `plan` message, and ends the run `waitingApproval`. Privileges and rules are checked when a change is made.

`approve` marks the plan executing and launches `PlanExecutor`: each change through its write tool as the editor, the references replaced by the ids the earlier ones produced, each outcome stored on the plan (`RecordPlanStepHandler`) and told as `planProgress`; the first failure skips the rest; a stop asked for skips the rest and ends there. Otherwise a `chat` run follows at once, so the assistant sums up or plans the rest. `cancel` drops a proposed plan and tells it. Every change is a transaction of its own; a plan is not one.

`FailUnfinishedRunsHandler` runs when the application is ready: a run still queued or running was ended by the restart and fails with an `error` message; a plan being made is interrupted — its running change fails without being made again, the rest are skipped.

### State and endpoint

`GetConversationStateHandler` answers the messages after a `seq`, every plan, the active run and `refs`: the entries mentioned as `[[type:id]]` in the messages, and in the changes of plans every id of a field named `id`, `<name>Id` or `<name>Ids`, every code of a field `hardware` and every id a change produced (`Mentions`), found by id alone through `JdbcEntityRefReadRepository` — a category too, leading to its catalog — with their Russian names and the paths of their pages; an id of nothing is left out.

`AssistantController` (`/api/assistant`): `GET /conversations`, `POST /conversations` (201), `GET /conversations/{id}?afterSeq=`, `POST /conversations/{id}/messages` (`text`, `page`: `path`, `title`, `entityType`, `entityId`; 202), `POST /conversations/{id}/stop` (204), `DELETE /conversations/{id}` (204), `POST /plans/{id}/approve` (202), `POST /plans/{id}/cancel` (204). Payloads, arguments and states are answered as JSON, not as strings.

Tests: the domain (`RunTest`, `PlanTest`, `ConversationTest`), `PlanReferencesTest`, `ConversationHistoryTest`, `OpenAiLanguageModelTest` (against a local server), `AssistantControllerTest`, and `AssistantChatIT`: the chat through `ChatService` with `ScriptedLanguageModel` and runs made at once in the test's thread and transaction (`AssistantTestConfiguration`).

## Panel

`features/assistant-chat`, in the main menu for a user with the `use-assistant` ability (`GET /api/auth/me`). `AssistantPanel` is its button and its `Drawer`; `AssistantChatProvider` around both keeps the chat while the panel is closed and the editor goes from page to page.

- The panel opens on a new conversation; the first message creates it (`POST /conversations`), then sends the text with the page (`pageContextOf`: the path with its query, so a list goes with its filters and page, the title of the tab and the entry the path shows; the routes of an entry are matched there). The title is followed as it changes (`useDocumentTitle`): a page names its tab when it renders (`PageLayout`), after the route changed and often once its data came. The list of conversations opens in place of the open one, which it replaces when one is picked.
- A conversation is one query (`conversationQuery`) read in parts: each read asks for the messages after the last one held (`afterSeq`) and lays the update over what is held (`withUpdate`: messages appended, plans and the active run replaced, refs added). It reads again every 2 s while there is an active run, and once after each request of the editor.
- `streamOf` folds the calls of tools in a row into one quiet line of steps; opened, each step reads as words of its own (`assistant.tools.<name>`, a tool without them by its name) with what it was about (the first of `phrase`, `keyword`, `word`, `name`, `title`, `url` among its arguments); a result starting with `Error:` is a failed step, its reason under it and leaves plan progress to the plans, which show the status of each change. `mentionSegments` turns `[[type:id]]` into links by `refs`; an id the refs lack stays as written.
- A plan shows each change by `actionTarget` (a link to the entry by `id`, its state's `id` or the id it produced) and `actionRows` (each argument but `id`, beside the state's field of that name when it differs; `{"$ref": N}` as made by change N). A field is named by `assistant.plan.fields.<name>`, one without words by its name; a value of a reference book by the words the interface has for it (`release.types`, `software.preservation`, `roles`); an item of a list as its values joined by "·", each item on a line, a flag set on it by `assistant.plan.flags.<name>`, the `type` of a credited entry left to its link. Make and Cancel are offered only while it is proposed and no run is active; Make asks first.
- A conversation or a plan that is gone (`404`) is not thrown to the route: `assistant-api` takes it, and the panel goes to a new conversation. Business errors of the requests are shown above the message; system errors go to the error dialog.
- Answers are shown as plain text, their lines kept; Markdown is not rendered.
