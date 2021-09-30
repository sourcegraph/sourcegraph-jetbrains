package hackathon;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.AppUIExecutor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.popup.JBPopup;
import org.jetbrains.annotations.NotNull;

public class OpenSourcegraphSearchBarAction extends AnAction implements DumbAware {

    private SearchResultsTreeView searchResultsTreeView;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
//        String query = Messages.showInputDialog("enter a search query", "Sourcegraph Search", null, "", null);
//        System.out.println(query);
//        if (StringUtils.isEmpty(query)) {
//            return;
//        }

        // todo make it so this window doesnt get destroyed on close immediately

        AppUIExecutor.onUiThread().execute(() -> {
            if (searchResultsTreeView == null || searchResultsTreeView.isDisposed()) {
                searchResultsTreeView = new SearchResultsTreeView(e.getProject());
            }
            JBPopup popup = searchResultsTreeView.createPopup();
            popup.showCenteredInCurrentWindow(ProjectManager.getInstance().getOpenProjects()[0]);
        });

//        sourcegraphClient.searchAsync(query, r -> {
//            AppUIExecutor.onUiThread().execute(() -> {
//                JBPopup popup = new SearchResultsTreeView(r).createPopup();
//                popup.showCenteredInCurrentWindow(ProjectManager.getInstance().getOpenProjects()[0]);
//            });
//        }, System.out::println);
    }
}
