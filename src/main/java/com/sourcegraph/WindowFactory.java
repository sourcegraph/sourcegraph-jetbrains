package com.sourcegraph;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

class WindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SourcegraphWindowService service = ServiceManager.getService(project, SourcegraphWindowService.class);
        System.out.println("+++service" + service.getClass().toString());

        SourcegraphWindow window = service.window;

        System.out.println("+++window" + window.toString());

        toolWindow.getComponent().getParent().add(window.getComponent());
    }


//    override def createToolWindowContent(project: Project, toolWindow: ToolWindow): Unit = {
//        val catViewerWindow = ServiceManager.getService(project, classOf[SourcegraphWindowService]).catViewerWindow;
//        val component = toolWindow.getComponent;
//        component.getParent.add(catViewerWindow.content);
//    }
}