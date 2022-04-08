package com.sourcegraph;

import com.intellij.ide.actions.CopyAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.ex.AnActionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.jcef.JBCefBrowser;

import javax.swing.*;

import com.intellij.ui.jcef.JBCefJSQuery;
import com.intellij.util.ui.UIUtil;
import net.minidev.json.JSONObject;
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
                .setUrl("file://C:\\Users\\hello\\IdeaProjects\\sourcegraph-jetbrains\\src\\main\\resources\\html\\index.html")
                .setOffScreenRendering(true)
                .createBrowser();


//        CefBrowser myDevTools = this.webView.getCefBrowser().getDevTools();
//        JBCefBrowser myDevToolsBrowser = new JBCefBrowser(myDevTools,
//                this.webView.getJBCefClient());
//
//        myDevToolsBrowser.openDevtools();


//        this.webView.loadURL(");


        String backgroundColor = "#"+Integer.toHexString(UIUtil.getPanelBackground().getRGB()).substring(2);

        this.webView.setPageBackgroundColor(backgroundColor);

        for (String action : ActionManager.getInstance().getActionIdList("")) {
            Shortcut[] shortcuts = ActionManager.getInstance().getAction(action).getShortcutSet().getShortcuts();
            for (Shortcut shortcut : shortcuts) {
                if(shortcut instanceof KeyboardShortcut){
                    System.out.println("[" + action + "]" + shortcut.toString());
                }
            }
        }

        Disposer.register(project, this.webView);

        JBCefJSQuery bridge = JBCefJSQuery.create(this.webView);
        bridge.addHandler((command) -> {
            if(command.startsWith("theme")) {
                // https://plugins.jetbrains.com/docs/intellij/themes-metadata.html#key-naming-scheme
                // System.out.println(UIManager.getColor("Label.background"));
                 System.out.println(UIManager.get("Button.arc"));
                // System.out.println();
                // System.out.println( UIUtil.getLabelFont().toString());

                JSONObject theme = new JSONObject();
                theme.put("backgroundColor", "#"+Integer.toHexString(UIUtil.getPanelBackground().getRGB()).substring(2));
                theme.put("font", UIUtil.getLabelFont().getFontName());
                theme.put("fontSize", UIUtil.getLabelFont().getSize());
                theme.put("color", "#"+Integer.toHexString(UIUtil.getLabelForeground().getRGB()).substring(2));

                System.out.println(theme.toJSONString());

                return new JBCefJSQuery.Response(theme.toJSONString());
            } else if(command.startsWith("popup")) {
                String[] args = command.split(":");

                SwingUtilities.invokeLater(() -> {
                    DefaultActionGroup actionGroup = new DefaultActionGroup();
                    actionGroup.addSeparator("test");
                    actionGroup.addSeparator("1234");
                    ActionPopupMenu actionPopupMenu =
                            ActionManager.getInstance().createActionPopupMenu("unknown",actionGroup) ;
                    actionPopupMenu.getComponent().show(
                            this.toolWindow.getComponent().getParent(), parseInt(args[1]),  parseInt(args[2])); // toolWindow is composed of only one Jpanel
                });
                return null;

            } else {
                System.out.println("F+++" + command);
                return null; // can respond back to JS with JBCefJSQuery.Response
            }
        });

        UIManager.addPropertyChangeListener( e -> {
            if(e.getPropertyName().equals("lookAndFeel")) {
                System.out.println("look and feel change ya'know");
                System.out.println(UIManager.getLookAndFeelDefaults());
                System.out.println( "#"+Integer.toHexString(UIUtil.getPanelBackground().getRGB()).substring(2));
                String bgc = "#"+Integer.toHexString(UIUtil.getPanelBackground().getRGB()).substring(2);
                this.webView.setPageBackgroundColor(bgc);
                this.webView.getCefBrowser().executeJavaScript("window.__sginit();", "", 0);
            }
        } );


        this.webView.createImmediately();
        SourcegraphBridgeThread thread = new SourcegraphBridgeThread(this.webView, bridge);
        thread.start();
    }

    public void setToolWindow(ToolWindow toolWindow) {
        this.toolWindow = toolWindow;
    }

    public JComponent getComponent() {
        return this.webView.getComponent();
    }

    public JBCefBrowser getBrowser() { return this.webView; }
}