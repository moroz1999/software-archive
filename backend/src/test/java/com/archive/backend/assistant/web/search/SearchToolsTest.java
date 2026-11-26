package com.archive.backend.assistant.web.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolJson;
import com.archive.backend.author.app.ListAuthorsHandler;
import com.archive.backend.author.app.ListAuthorsQuery;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.group.app.ListGroupsHandler;
import com.archive.backend.group.app.ListGroupsQuery;
import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelLetter;
import com.archive.backend.search.app.FullSearchHandler;
import com.archive.backend.search.app.FullSearchQuery;
import com.archive.backend.search.app.ListSoftwareCatalogHandler;
import com.archive.backend.search.app.ListSoftwareCatalogQuery;
import com.archive.backend.search.app.SearchReleasesHandler;
import com.archive.backend.search.app.SearchReleasesQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.FoundKind;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.search.read.FoundRelease;
import com.archive.backend.search.read.FullSearchResult;
import com.archive.backend.search.read.SoftwareCatalogAnswer;
import com.archive.backend.search.read.SoftwareCatalogFacets;
import com.archive.backend.search.read.SoftwareCatalogItem;
import com.archive.backend.search.read.SoftwareCatalogPage;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.app.ListLatestSoftwareHandler;
import com.archive.backend.software.app.ListLatestSoftwareQuery;
import com.archive.backend.software.app.LookupSoftwareReferencesHandler;
import com.archive.backend.software.app.LookupSoftwareReferencesQuery;
import com.archive.backend.software.domain.ReleaseFileHash;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReferenceId;
import com.archive.backend.software.domain.SoftwareReferenceType;
import com.archive.backend.software.read.SoftwareReferenceOption;
import com.archive.backend.software.read.SoftwareSummary;
import com.archive.backend.user.TestActors;
import com.jayway.jsonpath.DocumentContext;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

/**
 * The searches and lists of the assistant: the arguments become the query of the same use case the site's own page
 * asks, so the assistant reads a list as the editor sees it.
 */
class SearchToolsTest {

    private static final ToolContext CONTEXT = new ToolContext(TestActors.MEMBER, Language.RU);
    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Test
    void searchesTheKindsAskedOnTheirPageOfFifty() {
        FullSearchHandler handler = mock(FullSearchHandler.class);
        when(handler.handle(any())).thenReturn(new FullSearchResult(1, 50, 0, List.of()));

        new SearchTool(handler).read(CONTEXT, new SearchArguments("elite", List.of("author"), "vector", 2));

        FullSearchQuery query = captured(handler);
        assertThat(query.keyword()).isEqualTo(new CatalogKeyword("elite"));
        assertThat(query.platform()).isEqualTo(new PlatformCode("vector"));
        assertThat(query.kinds()).containsExactly(FoundKind.AUTHOR);
        assertThat(query.pageable()).isEqualTo(PageRequest.of(1, 50));
    }

    @Test
    void searchesEveryKindOnTheFirstPageWhenNoneIsAsked() {
        FullSearchHandler handler = mock(FullSearchHandler.class);
        when(handler.handle(any())).thenReturn(new FullSearchResult(0, 50, 7, List.of()));

        DocumentContext json =
                ToolJson.of(new SearchTool(handler).read(CONTEXT, new SearchArguments("elite", null, null, null)));

        FullSearchQuery query = captured(handler);
        assertThat(query.kinds()).containsExactlyInAnyOrder(FoundKind.AUTHOR, FoundKind.GROUP, FoundKind.SOFTWARE);
        assertThat(query.platform()).isNull();
        assertThat(query.pageable()).isEqualTo(PageRequest.of(0, 50));
        assertThat(json.read("$.total", Integer.class)).isEqualTo(7);
    }

    @Test
    void findsNamesOfEveryKindLabelsOfNobodyToo() {
        LookupSoftwareReferencesHandler handler = mock(LookupSoftwareReferencesHandler.class);
        when(handler.handle(any())).thenReturn(List.of(new SoftwareReferenceOption(ID, null, "author-label", "Ghost")));

        DocumentContext json =
                ToolJson.of(new FindNamesTool(handler).read(CONTEXT, new FindNamesArguments("gho", null)));

        ArgumentCaptor<LookupSoftwareReferencesQuery> query =
                ArgumentCaptor.forClass(LookupSoftwareReferencesQuery.class);
        verify(handler).handle(query.capture());
        assertThat(query.getValue().phrase().value()).isEqualTo("gho");
        assertThat(query.getValue().types()).containsExactlyInAnyOrder(SoftwareReferenceType.values());
        assertThat(json.read("$[0].title", String.class)).isEqualTo("Ghost");
        assertThat(json.read("$[0].type", String.class)).isEqualTo("author-label");
    }

    @Test
    void findsNamesOfTheKindsAsked() {
        LookupSoftwareReferencesHandler handler = mock(LookupSoftwareReferencesHandler.class);
        when(handler.handle(any())).thenReturn(List.of());

        new FindNamesTool(handler).read(CONTEXT, new FindNamesArguments("gho", List.of("group", "group-label")));

        ArgumentCaptor<LookupSoftwareReferencesQuery> query =
                ArgumentCaptor.forClass(LookupSoftwareReferencesQuery.class);
        verify(handler).handle(query.capture());
        assertThat(query.getValue().types())
                .containsExactlyInAnyOrder(SoftwareReferenceType.GROUP, SoftwareReferenceType.GROUP_LABEL);
    }

    @Test
    void readsTheSoftwareCatalogWithItsFiltersInTheLanguageOfTheEditor() {
        ListSoftwareCatalogHandler handler = mock(ListSoftwareCatalogHandler.class);
        when(handler.handle(any()))
                .thenReturn(new SoftwareCatalogAnswer(
                        new SoftwareCatalogPage(
                                new PageImpl<>(
                                        List.of(new SoftwareCatalogItem(ID, "Elite", "1987", List.of(), List.of())),
                                        PageRequest.of(2, 10),
                                        21),
                                new SoftwareCatalogFacets(List.of(), List.of())),
                        List.of()));

        DocumentContext json = ToolJson.of(new SearchSoftwareTool(handler)
                .read(
                        CONTEXT,
                        new SearchSoftwareArguments(
                                "elite", "vector", 1987, ID, ID, OTHER, List.of("bk"), List.of("ay8910"), 3, 10)));

        ArgumentCaptor<ListSoftwareCatalogQuery> captor = ArgumentCaptor.forClass(ListSoftwareCatalogQuery.class);
        verify(handler).handle(captor.capture());
        ListSoftwareCatalogQuery query = captor.getValue();
        assertThat(query.keyword()).isEqualTo(new CatalogKeyword("elite"));
        assertThat(query.platform()).isEqualTo(new PlatformCode("vector"));
        assertThat(query.year()).isEqualTo(new ReleaseYear(1987));
        assertThat(query.category()).isEqualTo(new CategoryId(ID));
        assertThat(query.country()).isEqualTo(new CountryId(ID));
        assertThat(query.city()).isEqualTo(new CityId(OTHER));
        assertThat(query.platforms()).containsExactly(new PlatformCode("bk"));
        assertThat(query.hardware()).containsExactly(new HardwareCode("ay8910"));
        assertThat(query.language()).isEqualTo(Language.RU);
        assertThat(query.pageable()).isEqualTo(PageRequest.of(2, 10));
        assertThat(json.read("$.total", Integer.class)).isEqualTo(21);
        assertThat(json.read("$.page", Integer.class)).isEqualTo(3);
        assertThat(json.read("$.size", Integer.class)).isEqualTo(10);
        assertThat(json.read("$.items[0].title", String.class)).isEqualTo("Elite");
    }

    @Test
    void readsTheWholeSoftwareCatalogOnItsFirstPage() {
        ListSoftwareCatalogHandler handler = mock(ListSoftwareCatalogHandler.class);
        when(handler.handle(any()))
                .thenReturn(new SoftwareCatalogAnswer(
                        new SoftwareCatalogPage(
                                new PageImpl<>(List.of(), PageRequest.of(0, 50), 0),
                                new SoftwareCatalogFacets(List.of(), List.of())),
                        List.of()));

        new SearchSoftwareTool(handler)
                .read(CONTEXT, new SearchSoftwareArguments(null, null, null, null, null, null, null, null, null, null));

        ArgumentCaptor<ListSoftwareCatalogQuery> captor = ArgumentCaptor.forClass(ListSoftwareCatalogQuery.class);
        verify(handler).handle(captor.capture());
        assertThat(captor.getValue().keyword()).isNull();
        assertThat(captor.getValue().platforms()).isEmpty();
        assertThat(captor.getValue().hardware()).isEmpty();
        assertThat(captor.getValue().pageable()).isEqualTo(PageRequest.of(0, ListSoftwareCatalogQuery.PAGE_SIZE));
    }

    @Test
    void keepsAPageBetweenOneAndAHundredEntries() {
        ListLatestSoftwareHandler handler = mock(ListLatestSoftwareHandler.class);
        when(handler.handle(any())).thenReturn(new PageImpl<SoftwareSummary>(List.of()));

        new ListLatestSoftwareTool(handler).read(CONTEXT, new ListLatestSoftwareArguments(null, 0, 1000));
        new ListLatestSoftwareTool(handler).read(CONTEXT, new ListLatestSoftwareArguments("vector", 2, 0));

        ArgumentCaptor<ListLatestSoftwareQuery> captor = ArgumentCaptor.forClass(ListLatestSoftwareQuery.class);
        verify(handler, org.mockito.Mockito.times(2)).handle(captor.capture());
        assertThat(captor.getAllValues().get(0).pageable()).isEqualTo(PageRequest.of(0, 100));
        assertThat(captor.getAllValues().get(0).platform()).isNull();
        assertThat(captor.getAllValues().get(1).pageable()).isEqualTo(PageRequest.of(1, 1));
        assertThat(captor.getAllValues().get(1).platform()).isEqualTo(new PlatformCode("vector"));
    }

    @Test
    void readsTheAuthorCatalogAsItsPageHasIt() {
        ListAuthorsHandler handler = mock(ListAuthorsHandler.class);
        when(handler.handle(any()))
                .thenReturn(new PageImpl<>(
                        List.of(new AuthorCatalogEntry(ID, null, "Jane Doe", "JD")), PageRequest.of(1, 100), 101));

        DocumentContext json = ToolJson.of(
                new ListAuthorsTool(handler).read(CONTEXT, new CatalogArguments("a", ID, OTHER, "vector", 2, 100)));

        ArgumentCaptor<ListAuthorsQuery> captor = ArgumentCaptor.forClass(ListAuthorsQuery.class);
        verify(handler).handle(captor.capture());
        ListAuthorsQuery query = captor.getValue();
        assertThat(query.letter()).isEqualTo(new LabelLetter("A"));
        assertThat(query.country()).isEqualTo(new CountryId(ID));
        assertThat(query.city()).isEqualTo(new CityId(OTHER));
        assertThat(query.platform()).isEqualTo(new PlatformCode("vector"));
        assertThat(query.pageable()).isEqualTo(PageRequest.of(1, 100));
        assertThat(json.read("$.total", Integer.class)).isEqualTo(101);
        assertThat(json.read("$.items[0].name", String.class)).isEqualTo("Jane Doe");
        assertThat(json.read("$.items[0].note", String.class)).isEqualTo("JD");
    }

    @Test
    void readsTheGroupCatalogAsItsPageHasIt() {
        ListGroupsHandler handler = mock(ListGroupsHandler.class);
        when(handler.handle(any())).thenReturn(new PageImpl<GroupCatalogEntry>(List.of()));

        new ListGroupsTool(handler).read(CONTEXT, new CatalogArguments("#", null, null, null, null, null));

        ArgumentCaptor<ListGroupsQuery> captor = ArgumentCaptor.forClass(ListGroupsQuery.class);
        verify(handler).handle(captor.capture());
        assertThat(captor.getValue().letter()).isEqualTo(new LabelLetter("#"));
        assertThat(captor.getValue().country()).isNull();
        assertThat(captor.getValue().pageable()).isEqualTo(PageRequest.of(0, ListGroupsQuery.PAGE_SIZE));
    }

    @Test
    void refusesALetterNoCatalogHas() {
        ListAuthorsHandler handler = mock(ListAuthorsHandler.class);

        assertThatThrownBy(() -> new ListAuthorsTool(handler)
                        .read(CONTEXT, new CatalogArguments("ab", null, null, null, null, null)))
                .isInstanceOf(InvalidValueException.class);
    }

    @Test
    void searchesReleasesByEveryConditionGiven() {
        SearchReleasesHandler handler = mock(SearchReleasesHandler.class);
        when(handler.handle(any()))
                .thenReturn(new PageImpl<>(
                        List.of(new FoundRelease(
                                ID, OTHER, "Elite", "vector", null, null, "crack", "1990", List.of(), null, null,
                                List.of())),
                        PageRequest.of(0, 20),
                        1));

        DocumentContext json = ToolJson.of(new SearchReleasesTool(handler)
                .read(
                        CONTEXT,
                        new SearchReleasesArguments(
                                "elite",
                                List.of(OTHER),
                                List.of("crack"),
                                1985,
                                1990,
                                "vector",
                                List.of("vector06c"),
                                List.of(ID),
                                List.of("release"),
                                "elite.rom",
                                "FEDCBA9876543210FEDCBA9876543210",
                                1,
                                20)));

        ArgumentCaptor<SearchReleasesQuery> captor = ArgumentCaptor.forClass(SearchReleasesQuery.class);
        verify(handler).handle(captor.capture());
        SearchReleasesQuery query = captor.getValue();
        assertThat(query.word()).isEqualTo(new CatalogKeyword("elite"));
        assertThat(query.software()).containsExactly(new SoftwareId(OTHER));
        assertThat(query.types()).containsExactly(ReleaseType.CRACK);
        assertThat(query.yearFrom()).isEqualTo(new ReleaseYear(1985));
        assertThat(query.yearTo()).isEqualTo(new ReleaseYear(1990));
        assertThat(query.platform()).isEqualTo(new PlatformCode("vector"));
        assertThat(query.hardware()).containsExactly(new HardwareCode("vector06c"));
        assertThat(query.credited()).containsExactly(new SoftwareReferenceId(ID));
        assertThat(query.roles()).containsExactly("release");
        assertThat(query.fileName()).isEqualTo(new CatalogKeyword("elite.rom"));
        assertThat(query.md5()).isEqualTo(new ReleaseFileHash("fedcba9876543210fedcba9876543210"));
        assertThat(query.pageable()).isEqualTo(PageRequest.of(0, 20));
        assertThat(json.read("$.total", Integer.class)).isEqualTo(1);
        assertThat(json.read("$.items[0].softwareTitle", String.class)).isEqualTo("Elite");
    }

    @Test
    void searchesEveryReleaseWhenNothingIsGiven() {
        SearchReleasesHandler handler = mock(SearchReleasesHandler.class);
        when(handler.handle(any())).thenReturn(new PageImpl<FoundRelease>(List.of()));

        new SearchReleasesTool(handler)
                .read(
                        CONTEXT,
                        new SearchReleasesArguments(
                                null, null, null, null, null, null, null, null, null, null, null, null, null));

        ArgumentCaptor<SearchReleasesQuery> captor = ArgumentCaptor.forClass(SearchReleasesQuery.class);
        verify(handler).handle(captor.capture());
        assertThat(captor.getValue())
                .isEqualTo(new SearchReleasesQuery(
                        null,
                        Set.of(),
                        Set.of(),
                        null,
                        null,
                        null,
                        Set.of(),
                        Set.of(),
                        Set.of(),
                        null,
                        null,
                        PageRequest.of(0, 50)));
    }

    private static FullSearchQuery captured(FullSearchHandler handler) {
        ArgumentCaptor<FullSearchQuery> query = ArgumentCaptor.forClass(FullSearchQuery.class);
        verify(handler).handle(query.capture());
        return query.getValue();
    }
}
