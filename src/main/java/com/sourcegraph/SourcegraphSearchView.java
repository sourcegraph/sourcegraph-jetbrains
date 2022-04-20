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
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
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