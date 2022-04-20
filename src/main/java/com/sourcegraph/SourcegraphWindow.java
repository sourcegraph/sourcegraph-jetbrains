package com.sourcegraph;

import com.intellij.ide.actions.CopyAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ClassLoaderUtil;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.jcef.JBCefBrowser;

import javax.swing.*;

import com.intellij.ui.jcef.JBCefJSQuery;
import com.intellij.util.ui.UIUtil;
import net.minidev.json.JSONObject;
import org.cef.CefApp;
import org.cef.browser.CefBrowser;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static java.lang.Integer.parseInt;

public class SourcegraphWindow {
    private Project project;

    private JBCefBrowser webView;
    private ToolWindow toolWindow;

    SourcegraphWindow(Project project) {
        this.project = project;

        this.webView = JBCefBrowser.createBuilder()
                .setUrl("http://sourcegraph/index.html")
                .setOffScreenRendering(true)
                .createBrowser();

        CefApp
                .getInstance()
                .registerSchemeHandlerFactory(
                        "http",
                        "sourcegraph",
                        new SourcegraphSchemeHandlerFactory()
                );

        String backgroundColor = "#"+Integer.toHexString(UIUtil.getPanelBackground().getRGB()).substring(2);

        this.webView.setPageBackgroundColor(backgroundColor);

        Disposer.register(project, this.webView);

        this.webView.createImmediately();
    }

    public void setToolWindow(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    public JComponent getComponent() {
        return this.webView.getComponent();
    }

    public JBCefBrowser getBrowser() { return this.webView; }
}