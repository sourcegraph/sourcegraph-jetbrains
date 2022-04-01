package com.sourcegraph;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

class WindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
//        SourcegraphWindowService service = ServiceManager.getService(project, SourcegraphWindowService.class);
//        SourcegraphWindow window = service.window;
//
//        window.setToolWindow(toolWindow);
//
//        toolWindow.getComponent().getParent().add(window.getComponent());
    }
}