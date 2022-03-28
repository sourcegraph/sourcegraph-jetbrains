package com.sourcegraph;

import com.intellij.openapi.project.Project;

public class SourcegraphWindowService {
    private Project project;
    public SourcegraphWindow window;
    public SourcegraphWindowService(Project project) {
        System.out.println("+++hi");
        this.project = project;
        this.window = new SourcegraphWindow(project);
    }
}