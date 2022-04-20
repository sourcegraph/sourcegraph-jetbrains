package com.sourcegraph;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.AppUIExecutor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

public class OpenSourcegraphSearchBarAction extends AnAction implements DumbAware {

    private SourcegraphSearchView searchResultsTreeView;

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        AppUIExecutor.onUiThread().execute(() -> {
            if (searchResultsTreeView == null || searchResultsTreeView.isDisposed()) {
                searchResultsTreeView = new SourcegraphSearchView(e.getProject());
            }
            searchResultsTreeView.createPopup().showCenteredInCurrentWindow(ProjectManager.getInstance().getOpenProjects()[0]);
        });
    }
}