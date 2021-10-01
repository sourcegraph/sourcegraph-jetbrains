package hackathon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.AppUIExecutor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ProjectManager;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

public class OpenSourcegraphSearchBarAction extends AnAction implements DumbAware {

    private SourcegraphSearchView searchResultsTreeView;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//        String query = Messages.showInputDialog("enter a search query", "Sourcegraph Search", null, "", null);
//        System.out.println(query);
//        if (StringUtils.isEmpty(query)) {
//            return;
//        }

        AppUIExecutor.onUiThread().execute(() -> {
            if (searchResultsTreeView == null || searchResultsTreeView.isDisposed()) {
                searchResultsTreeView = new SourcegraphSearchView(e.getProject());
//                popup = searchResultsTreeView.createPopup();
            }
            Editor editor = e.getRequiredData(CommonDataKeys.EDITOR);
            if (StringUtils.isNotEmpty(editor.getSelectionModel().getSelectedText())) {
                String query = editor.getSelectionModel().getSelectedText();
                searchResultsTreeView.doSearch(query);
            }
            searchResultsTreeView.createPopup().showCenteredInCurrentWindow(ProjectManager.getInstance().getOpenProjects()[0]);
        });

//        sourcegraphClient.searchAsync(query, r -> {
//            AppUIExecutor.onUiThread().execute(() -> {
//                JBPopup popup = new SearchResultsTreeView(r).createPopup();
//                popup.showCenteredInCurrentWindow(ProjectManager.getInstance().getOpenProjects()[0]);
//            });
//        }, System.out::println);
    }
}
