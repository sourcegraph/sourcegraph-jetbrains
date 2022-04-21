package com.sourcegraph;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.DumbAwareToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.*;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefKeyboardHandler;
import org.cef.misc.BoolRef;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.atomic.AtomicBoolean;

public class SourcegraphSearchView implements Disposable {

    private final Project project;

    private final JPanel panel;
    private final Tree tree;
    private boolean isDisposed = false;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel model;
    SourcegraphWindow window;
    private SimpleColoredComponent pathInfoTitle;


    public SourcegraphSearchView(Project project) {
        this.project = project;

        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(JBUI.size(1200, 800));
        panel.setBorder(PopupBorder.Factory.create(true, true));

        root = new DefaultMutableTreeNode("Match Type");
        model = new DefaultTreeModel(root);

        tree = new Tree(model);

        JPanel topPanel = new JPanel(new BorderLayout());


        pathInfoTitle = new SimpleColoredComponent();
        pathInfoTitle.setBorder(JBUI.Borders.empty(3, 8, 4, 8));

//        JPanel previewPanel = new JPanel(new BorderLayout());
//        previewPanel.add(pathInfoTitle, BorderLayout.CENTER);

//        Splitter splitter = new OnePixelSplitter(true, 0.5f, 0.1f, 0.9f);
//        splitter.setFirstComponent(topPanel);
//
//        splitter.setSecondComponent(previewPanel);



        SourcegraphWindowService service = ServiceManager.getService(project, SourcegraphWindowService.class);
        this.window = service.window;


        topPanel.add(this.window.getComponent());

        JBCefBrowser browser = this.window.getBrowser();
        JBCefClient client = browser.getJBCefClient();

        client.addKeyboardHandler(new CefKeyboardHandler() {
            @Override
            public boolean onPreKeyEvent(CefBrowser browser, CefKeyEvent event, BoolRef is_keyboard_shortcut) {
                System.out.println("onPreKeyEvent");
                System.out.println(event);
                return true;
            }

            @Override
            public boolean onKeyEvent(CefBrowser browser, CefKeyEvent event) {
                System.out.println("onKeyEvent");
                System.out.println(event);
                if(event.windows_key_code == 8 && event.modifiers == 0) {
                    System.out.println("SIMULATOR");
                    browser.executeJavaScript(
                            "(function backspace(){\n" +
                                    "var textbox = document.activeElement;\n" +
                            "var ss = textbox.selectionStart;\n" +
                            "var se = textbox.selectionEnd;\n" +
                            "var ln  = textbox.value.length;\n" +
                            "\n" +
                            "var textbefore = textbox.value.substring( 0, ss );    //text in front of selected text\n" +
                            "var textselected = textbox.value.substring( ss, se ); //selected text\n" +
                            "var textafter = textbox.value.substring( se, ln );    //text following selected text\n" +
                            "\n" +
                            "if(ss==se) // if no text is selected\n" +
                            "{\n" +
                            "textbox.value = textbox.value.substring(0, ss-1 ) + textbox.value.substring(se, ln );\n" +
                            "textbox.focus();\n" +
                            "textbox.selectionStart = ss-1;\n" +
                            "textbox.selectionEnd = ss-1;\n" +
                            "}\n" +
                            "else // if some text is selected\n" +
                            "{\n" +
                            "textbox.value = textbefore + textafter ;\n" +
                            "textbox.focus();\n" +
                            "textbox.selectionStart = ss;\n" +
                            "textbox.selectionEnd = ss;\n" +
                            "}\n" +
                            "\n" +
                            "})();","", 0);
                }
                return false;
            }
        }, browser.getCefBrowser());

        panel.add(topPanel, BorderLayout.CENTER);
        tree.setRootVisible(false);
        panel.setFocusCycleRoot(true);

    }

    public boolean isDisposed() {
        return isDisposed;
    }


    private void customizeEditorSettings(EditorSettings settings) {
        settings.setLineMarkerAreaShown(true);
        settings.setFoldingOutlineShown(false);
        settings.setAdditionalColumnsCount(0);
        settings.setAdditionalLinesCount(0);
        settings.setAnimatedScrolling(false);
        settings.setAutoCodeFoldingEnabled(false);
    }


    public JBPopup createPopup() {
//        return JBPopupFactory.getInstance().createComponentPopupBuilder(panel, searchTextArea.getTextArea())
        JBPopup popup =  JBPopupFactory.getInstance().createComponentPopupBuilder(panel, panel)
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

    public void show() {

    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    private final class ToggleAction extends DumbAwareToggleAction {
        private final AtomicBoolean state;

        public ToggleAction(@Nullable @Nls(capitalization = Nls.Capitalization.Title) String text, @Nullable @Nls(capitalization = Nls.Capitalization.Sentence) String description, @Nullable Icon icon, Icon hoveredIcon, Icon selectedIcon, AtomicBoolean atomicBoolean) {
            super(text, description, icon);
            state = atomicBoolean;
            getTemplatePresentation().setHoveredIcon(hoveredIcon);
            getTemplatePresentation().setSelectedIcon(selectedIcon);
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return state.get();
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            this.state.set(state);
        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            System.out.println(state.get());
            Toggleable.setSelected(e.getPresentation(), state.get());
        }
    }
}