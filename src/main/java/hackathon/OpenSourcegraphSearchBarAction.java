package hackathon;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.AppUIExecutor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopup;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OpenSourcegraphSearchBarAction extends AnAction implements DumbAware {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        String query = Messages.showInputDialog("enter a search query", "Sourcegraph Search", null, "", null);
        System.out.println(query);
        if (StringUtils.isEmpty(query)) {
            return;
        }

        Project project = e.getProject();

        ApolloClient apolloClient = ApolloClient.builder()
                .serverUrl("https://sourcegraph.com/.api/graphql")
                .build();

        apolloClient.query(new SearchQuery(query)).enqueue(new ApolloCall.Callback<SearchQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<SearchQuery.Data> response) {
                List<SearchResult> results = new ArrayList<>();
                for (SearchQuery.Result result : response.getData().search().results().results()) {
                    results.addAll(parse(result));
                }
//                System.out.println(response);
                System.out.println("search complete");

                AppUIExecutor.onUiThread().execute(() -> {
//                    editor = editorFromSearchResult(srn.getSearchResult());
//                    jbPanelWithEmptyText.removeAll();
//                    jbPanelWithEmptyText.add(editor.getComponent(), BorderLayout.CENTER);
//                    jbPanelWithEmptyText.revalidate();
                    JBPopup popup = new SearchResultsTreeView(results).createPopup();
                    popup.showCenteredInCurrentWindow(project);
                });

//                ApplicationManager.getApplication().invokeLaterOnWriteThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        ListPopup popup = JBPopupFactory.getInstance().createListPopup(new BaseListPopupStep<>("results", results));
//                        JBPopup popup = new SearchResultsTreeView(results).createPopup();
//                        popup.showCenteredInCurrentWindow(project);
//                    }
//                });
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {

            }
        });
//        String raw = "{\"query\":\"query Search(\\n\\t$query: String!,\\n) {\\n\\tsearch(query: $query, version: V2, patternType:literal) {\\n\\t\\tresults {\\n\\t\\t\\tlimitHit\\n\\t\\t\\tcloning { name }\\n\\t\\t\\tmissing { name }\\n\\t\\t\\ttimedout { name }\\n\\t\\t\\tmatchCount\\n\\t\\t\\tresults {\\n\\t\\t\\t\\t__typename\\n\\t\\t\\t\\t... on FileMatch {\\n\\t\\t\\t\\t\\trepository {\\n\\t\\t\\t\\t\\t\\tid\\n\\t\\t\\t\\t\\t\\tname\\n\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t\\tlineMatches {\\n\\t\\t\\t\\t\\t\\toffsetAndLengths\\n\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t\\tsymbols {\\n\\t\\t\\t\\t\\t\\tname\\n\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t}\\n\\t\\t\\t\\t... on CommitSearchResult {\\n\\t\\t\\t\\t\\tmatches {\\n\\t\\t\\t\\t\\t\\thighlights {\\n\\t\\t\\t\\t\\t\\t\\tline\\n\\t\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t\\tcommit {\\n\\t\\t\\t\\t\\t\\trepository {\\n\\t\\t\\t\\t\\t\\t\\tid\\n\\t\\t\\t\\t\\t\\t\\tname\\n\\t\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t\\t}\\n\\t\\t\\t\\t}\\n\\t\\t\\t\\t... on Repository {\\n\\t\\t\\t\\t\\tid\\n\\t\\t\\t\\t\\tname\\n\\t\\t\\t\\t}\\n\\t\\t\\t}\\n\\t\\t\\talert {\\n\\t\\t\\t\\ttitle\\n\\t\\t\\t\\tdescription\\n\\t\\t\\t}\\n\\t\\t}\\n\\t}\\n}\",\"variables\":{\"query\":\"file:spring-(\\\\w*)\\\\.properties\"},\"operationName\":\"Search\"}";
    }

    private List<SearchResult> parse(SearchQuery.Result result) {
        if (result instanceof SearchQuery.AsFileMatch) {
            return parse((SearchQuery.AsFileMatch) result);
        }
        if (result instanceof SearchQuery.AsRepository) {
            return parse((SearchQuery.AsRepository) result);
        }
        else {
            throw new IllegalArgumentException("GTFO");
        }
    }

    private List<SearchResult> parse(SearchQuery.AsFileMatch result) {
        List<SearchResult> results = new ArrayList<>();

        String repo = result.repository().name();
        String file = result.file().path();
        String type = "file";

        for (SearchQuery.LineMatch lineMatch : result.lineMatches()) {
            SearchResult sr = new SearchResult();

            sr.repo = repo;
            sr.file = file;
            sr.type = type;
            sr.preview = lineMatch.preview();
            sr.content = result.file.content;
            sr.offsetAndLength = getOffset(lineMatch);
            results.add(sr);
        }

        return results;
    }

    private OffsetAndLength getOffset(SearchQuery.LineMatch lineMatch) {
        for (List<Integer> outer : lineMatch.offsetAndLengths) {
            int offset = outer.get(0);
            int length = outer.get(1);
            return new OffsetAndLength(offset, length);
        }
        return new OffsetAndLength(0, 0);
    }

    private List<SearchResult> parse(SearchQuery.AsRepository result) {
        SearchResult sr = new SearchResult();
        sr.repo = result.name();
        sr.type = "repository";
        return Collections.singletonList(sr);
    }
}
