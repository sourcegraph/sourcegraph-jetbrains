package com.sourcegraph;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.find.SearchTextArea;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ShortcutSet;
import com.intellij.openapi.actionSystem.Toggleable;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.AppUIExecutor;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAwareToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SourcegraphSearchView implements Disposable {

    private final Project project;

    private final JPanel panel;
//    private final Tree tree;
    private boolean isDisposed = false;
//    private DefaultMutableTreeNode root;
//    private DefaultTreeModel model;
//    private SearchTextArea searchTextArea;
//    private JLabel matchCountLabel;
//    private SimpleColoredComponent pathInfoTitle;

//    private JBPanel editorPanel;
//    private JPanel titlePanel;
//
//    private JLabel errorLabel;
//    private JLabel loadingLabel;

    private AtomicBoolean caseSensitiveSelected = new AtomicBoolean(false);
    private AtomicBoolean regexSelected = new AtomicBoolean(false);
    private AtomicBoolean structuralSelected = new AtomicBoolean(false);

    private static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private static final KeyStroke SHIFT_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);


    private VirtualFile openInPreview;

    private ExclusiveToggleActionGroup exclusiveToggleActionGroup;

    public SourcegraphSearchView(Project project) {
        this.project = project;

        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(JBUI.size(1200, 800));
        panel.setBorder(PopupBorder.Factory.create(true, true));

//        root = new DefaultMutableTreeNode("Match Type");
//        model = new DefaultTreeModel(root);
//
//        tree = new Tree(model);
//
//        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(tree);
//
//        pathInfoTitle = new SimpleColoredComponent();
//        pathInfoTitle.setBorder(JBUI.Borders.empty(3, 8, 4, 8));
//



        SourcegraphWindowService service = ServiceManager.getService(project, SourcegraphWindowService.class);
        SourcegraphWindow window = service.window;

        panel.add(window.getComponent());


    }

    public boolean isDisposed() {
        return isDisposed;
    }




    public JBPopup createPopup() {
//        return JBPopupFactory.getInstance().createComponentPopupBuilder(panel, searchTextArea.getTextArea())
        return JBPopupFactory.getInstance().createComponentPopupBuilder(panel, panel)
                .setTitle("Sourcegraph Search Results")
                .setCancelOnClickOutside(false)
                .setResizable(true)
                .setModalContext(false)
                .setRequestFocus(true)
                .setFocusable(true)
                .setMovable(true)
                .setBelongsToGlobalPopupStack(true)
                .setCancelOnOtherWindowOpen(false)
                .setCancelKeyEnabled(true)
                .setNormalWindowLevel(true)
                .createPopup();
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

            ShortcutSet mnemonicAsShortcut = ActionUtil.getMnemonicAsShortcut(this);
            if (mnemonicAsShortcut != null) {
                System.out.println(mnemonicAsShortcut);
                setShortcutSet(mnemonicAsShortcut);
//                registerCustomShortcutSet(mnemonicAsShortcut, searchTextArea);
            }
        }

        @Override
        public boolean isSelected(@NotNull AnActionEvent e) {
            return state.get();
        }

        @Override
        public void setSelected(@NotNull AnActionEvent e, boolean state) {
            this.state.set(state);
//            if (state) {
//                exclusiveToggleActionGroup.ensureExclusive(this, e);
//            }

        }

        @Override
        public void update(@NotNull AnActionEvent e) {
            System.out.println(state.get());
            Toggleable.setSelected(e.getPresentation(), state.get());
        }
    }
    class ExclusiveToggleActionGroup {
        private final List<ToggleAction> actions;

        ExclusiveToggleActionGroup(List<ToggleAction> actions) {
            this.actions = actions;
        }

        public void ensureExclusive(AnAction actionToKeep, AnActionEvent event) {
            for (ToggleAction action : actions) {
                if (action == actionToKeep) {
                    System.out.printf("toKeep: %s current: %s", actionToKeep, action);
                    continue;
                }
                action.setSelected(event, false);
                action.update(event);
            }
        }
    }
}