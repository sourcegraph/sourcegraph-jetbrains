package com.sourcegraph;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.PopupBorder;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import com.intellij.util.ui.JBUI;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefKeyboardHandler;
import org.cef.misc.BoolRef;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class SourcegraphSearchView implements Disposable {

    private final JPanel panel;
    private boolean isDisposed = false;
    SourcegraphWindow window;


    public SourcegraphSearchView(Project project) {
        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(JBUI.size(1200, 800));
        panel.setBorder(PopupBorder.Factory.create(true, true));

        JPanel topPanel = new JPanel(new BorderLayout());


        SimpleColoredComponent pathInfoTitle = new SimpleColoredComponent();
        pathInfoTitle.setBorder(JBUI.Borders.empty(3, 8, 4, 8));

        SourcegraphWindowService service = ServiceManager.getService(project, SourcegraphWindowService.class);
        this.window = service.window;


        topPanel.add(this.window.getComponent());

        JBCefBrowser browser = this.window.getBrowser();
        JBCefClient client = browser.getJBCefClient();

        //System.out.println(Arrays.toString(panel.getInputMap().allKeys()));
        //System.out.println(Arrays.toString(panel.getActionMap().allKeys()));
//        panel.addKeyListener(new KeyListener() {
//            @Override
//            public void keyTyped(KeyEvent e) {
//                int key = e.getKeyCode();
//                System.out.println("Typed key: " + key);
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e) {
//                int key = e.getKeyCode();
//                System.out.println("Released key: " + key);
//            }
//
//            @Override
//            public void keyPressed(KeyEvent e) {
//                int key = e.getKeyCode();
//                System.out.println("Pressed Key: " + key);
//            }
//        });

        client.addKeyboardHandler(new CefKeyboardHandler() {
            @Override
            public boolean onPreKeyEvent(CefBrowser browser, CefKeyEvent event, BoolRef is_keyboard_shortcut) {
                return true;
            }

            @Override
            public boolean onKeyEvent(CefBrowser browser, CefKeyEvent event) {
                if (event.type == CefKeyEvent.EventType.KEYEVENT_KEYUP && event.windows_key_code == 8 && event.modifiers == 0) {
                    browser.sendKeyEvent(new KeyEvent(browser.getUIComponent(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_BACK_SPACE, '\b'));
                }
                return false;
            }
        }, browser.getCefBrowser());

        panel.add(topPanel, BorderLayout.CENTER);
        panel.setFocusCycleRoot(true);

    }

    public boolean isDisposed() {
        return isDisposed;
    }

    public JBPopup createPopup() {
//        return JBPopupFactory.getInstance().createComponentPopupBuilder(panel, searchTextArea.getTextArea())
        JBPopup popup = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, panel)
                .setTitle("Sourcegraph Search Results")
                .setCancelOnClickOutside(false)
                .setResizable(true)
                .setModalContext(false)
                .setRequestFocus(true)
                .setFocusable(true)
                .setMovable(true)
                .setBelongsToGlobalPopupStack(true)
                .setCancelOnOtherWindowOpen(true)
                .setCancelKeyEnabled(true)
                .setNormalWindowLevel(true)
                .createPopup();

        this.window.getBrowser().getCefBrowser().setFocus(true);
        return popup;
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }
}
