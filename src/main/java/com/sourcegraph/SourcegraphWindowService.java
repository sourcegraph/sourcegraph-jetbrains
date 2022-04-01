package com.sourcegraph;

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.openapi.project.Project;

public class SourcegraphWindowService {
    private Project project;
    public SourcegraphWindow window;
    public SourcegraphWindowService(Project project) {
        this.project = project;
        this.window = new SourcegraphWindow(project);
    }
}