package com.sourcegraph;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.jcef.JBCefApp;
import com.intellij.ui.jcef.JBCefBrowser;
import javax.swing.JComponent;
import org.cef.CefApp;

public class SourcegraphWindow {
    private Project project;
    private JBCefBrowser webView;

    SourcegraphWindow(Project project) {
        this.project = project;

        if (!JBCefApp.isSupported()) {
            // Fallback to an alternative browser-less solution
            System.out.println("xxxxx OMG");
            System.exit(123);
        }



        this.webView =  new JBCefBrowser("https://philippspiess.com/");
        this.webView.openDevtools();

//        this.registerAppSchemeHandler();
//        this.webView.loadURL("https://philippspiess.com/");
        Disposer.register(project, this.webView);


    }

    public JComponent getComponent() {
        return this.webView.getComponent();
    }


//    private void registerAppSchemeHandler() {
//        CefApp
//                .getInstance()
//                .registerSchemeHandlerFactory(
//                        "http",
//                        "myapp",
//                        new CustomSchemeHandlerFactory
//                )
//    }
}