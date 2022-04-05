package com.sourcegraph;

import com.intellij.openapi.util.Disposer;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefJSQuery;

public class SourcegraphBridgeThread extends Thread{
    private JBCefBrowser webView;
    private JBCefJSQuery bridge;

    SourcegraphBridgeThread(JBCefBrowser webView,JBCefJSQuery bridge) {
        this.webView=webView;
        this.bridge = bridge;
    }

    public void run() {
        System.out.println(this.webView.getCefBrowser().getFrameNames());
        System.out.println(this.webView.getCefBrowser().isLoading());
        // Wait for the website to load maybe?
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("----connect them bridges");
        this.webView.getCefBrowser().executeJavaScript(
                "window.__sgbridge = async function(command) { " +
                        " return new Promise((resolve, reject) => { " +
                        bridge.inject("command", "resolve", "reject") +
                        " });" +
                        "};",
                "", 0);
        this.webView.getCefBrowser().executeJavaScript(
                "window.__sginit();",
                "", 0);
        System.out.println("----no more bridges");
        Disposer.register(bridge, this.webView);
    }

}
