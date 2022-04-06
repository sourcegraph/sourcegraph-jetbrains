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
    private final Tree tree;
    private boolean isDisposed = false;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel model;
    SourcegraphWindow window;
    private final EditorFactory editorFactory;
//    private SearchTextArea searchTextArea;
//    private JLabel matchCountLabel;
    private SimpleColoredComponent pathInfoTitle;

    private JBPanel editorPanel;
//    private JPanel titlePanel;

//    private JLabel errorLabel;
//    private JLabel loadingLabel;

//    private AtomicBoolean caseSensitiveSelected = new AtomicBoolean(false);
//    private AtomicBoolean regexSelected = new AtomicBoolean(false);
//    private AtomicBoolean structuralSelected = new AtomicBoolean(false);

    private static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private static final KeyStroke SHIFT_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);


    private VirtualFile openInPreview;

    private ExclusiveToggleActionGroup exclusiveToggleActionGroup;

    public SourcegraphSearchView(Project project) {
        this.project = project;

        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(JBUI.size(1200, 800));
        panel.setBorder(PopupBorder.Factory.create(true, true));

        root = new DefaultMutableTreeNode("Match Type");
        model = new DefaultTreeModel(root);

        tree = new Tree(model);

//        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(tree);
        JPanel topPanel = new JPanel(new BorderLayout());


        pathInfoTitle = new SimpleColoredComponent();
        pathInfoTitle.setBorder(JBUI.Borders.empty(3, 8, 4, 8));

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(pathInfoTitle, BorderLayout.NORTH);

        Splitter splitter = new OnePixelSplitter(true, 0.5f, 0.1f, 0.9f);
        splitter.setFirstComponent(topPanel);

        editorFactory = EditorFactory.getInstance();

        editorPanel = new JBPanelWithEmptyText(new BorderLayout());
        previewPanel.add(editorPanel);

        splitter.setSecondComponent(previewPanel);

        JBTextArea searchTextComponent = new JBTextArea();

//        String[] keywords = new String[]{"repo:", "-repo:", "rev:", "repogroup:", "file:", "-file:", "content:", "-content:", "select:repo", "select:file", "select:content", "lang:", "-lang:", "type:", "case:yes", "fork:yes", "fork:only", "archived:yes", "archived:only", "count:", "count:all", "timeout:", "patternType:literal", "patternType:regexp", "patternType:structural", "visibility:any", "visibility:public", "visibility:private", "AND", "OR", "NOT"};

//        TextFieldWithAutoCompletion<String> editorTextField = TextFieldWithAutoCompletion.create(project, Set.of(keywords), true, "");

//        EditorTextField editorTextField = new EditorTextField("test");
//        editorTextField.setOneLineMode(true);
//        editorTextField.addNotify();
//        editorTextField.getEditor().get;

//        searchTextArea = new SearchTextArea(searchTextComponent, true);
//        searchTextComponent.setRows(1);
//        searchTextArea.setBorder(new CompoundBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.BigPopup.searchFieldBorderColor(), 1, 0, 1, 0),
//                JBUI.Borders.empty(1, 0, 2, 0)));


//        ToggleAction[] toggleActions = new ToggleAction[]{
//                new ToggleAction("Case Sensitive", "Use case sensitive search", AllIcons.Actions.MatchCase, AllIcons.Actions.MatchCaseHovered, AllIcons.Actions.MatchCaseSelected, caseSensitiveSelected),
//                new ToggleAction("Regex Search", "Use regex search", AllIcons.Actions.Regex, AllIcons.Actions.RegexHovered, AllIcons.Actions.RegexSelected, regexSelected),
//                new ToggleAction("Structural Search", "Use structural search", AllIcons.Actions.Words, AllIcons.Actions.WordsHovered, AllIcons.Actions.WordsSelected, structuralSelected)
//        };
        // this isn't working - the idea was to disable the other actions like a buttongroup would, but for some reason it isn't!
//        exclusiveToggleActionGroup = new ExclusiveToggleActionGroup(List.of(toggleActions));

//        searchTextArea.setExtraActions(toggleActions);
//        ButtonGroup buttonGroup = new ButtonGroup();
//        UIUtil.findComponentsOfType(searchTextArea, ActionButton.class).forEach(b -> {
//            System.out.println("x");
//            buttonGroup.add(b.getRootPane().getDefaultButton());
//        });

//        searchTextArea.setMultilineEnabled(false);
//        titlePanel = new JPanel(new MigLayout("flowx, ins 0, gap 0, fillx, filly"));
//        JLabel myTitleLabel = new JBLabel("Find on Sourcegraph", UIUtil.ComponentStyle.REGULAR);
//        RelativeFont.BOLD.install(myTitleLabel);
//        matchCountLabel = new JBLabel("", UIUtil.ComponentStyle.SMALL);
//        titlePanel.add(myTitleLabel, "gapright 4, gapleft 2");
//        titlePanel.add(matchCountLabel);

//        JPanel buttonPanel = new JPanel(new BorderLayout());
//        buttonPanel.setBorder(new CompoundBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.BigPopup.searchFieldBorderColor(), 0, 0, 0, 0),
//                JBUI.Borders.empty(1, 0)));
//


        SourcegraphWindowService service = ServiceManager.getService(project, SourcegraphWindowService.class);
        this.window = service.window;


        topPanel.add(this.window.getComponent());

//        topPanel.add(titlePanel);
//        topPanel.add(editorTextField);
//        JTextField jTextField = new JTextField();
//        jTextField.setBorder(null);
//        topPanel.add(jTextField);
//        topPanel.add(buttonPanel);


//        JPanel contentPanel = new JPanel(new BorderLayout());
//        contentPanel.add(splitter, BorderLayout.CENTER);

//        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(splitter, BorderLayout.CENTER);

        // its magic
//        TreeSpeedSearch treeSpeedSearch = new TreeSpeedSearch(tree);

//        errorLabel = new JLabel();
//        errorLabel.setText("search error");
//        errorLabel.setForeground(JBColor.RED);
//        RelativeFont.BOLD.install(errorLabel);

//
//        loadingLabel = new JLabel();
//        titlePanel.add(loadingLabel, "w 24, wmin 24, gapleft 2");


        ActionListener openInSplitEditorAction = e -> {
            if (openInPreview == null) {
                return;
            }
            AppUIExecutor.onUiThread().execute(() -> {
                FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, openInPreview, 0), false);
            });
        };

        tree.setRootVisible(false);

//        searchTextArea.registerKeyboardAction(searchAction,KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        tree.registerKeyboardAction(openInSplitEditorAction, SHIFT_ENTER, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

//        panel.setFocusTraversalPolicy(new ListFocusTraversalPolicy(List.of(searchTextComponent, tree)));
        panel.setFocusCycleRoot(true);

        this.editorFromSearchResult();
    }

    public void doSearch(String query) {
        AppUIExecutor.onUiThread().execute(() -> {
            this.editorFromSearchResult();
        });
    }

    public boolean isDisposed() {
        return isDisposed;
    }


    private void editorFromSearchResult() {
        String contentJava = "import com.intellij.ui.components.SomethingThatIsMadeUp;\n" +
                "\n" +
                "class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        SomethingThatIsMadeUp.test();\n" +
                "        System.out.println(\"Hello, World!\"); \n" +
                "    }\n" +
                "}";
        String contentTs = "let message: string = 'Hello, TypeScript!';\n" +
                "\n" +
                "let heading = document.createElement('h1');\n" +
                "heading.textContent = message;\n" +
                "\n" +
                "document.body.appendChild(heading);";
        System.out.println("search result to editor");
        VirtualFile virtualFile = new LightVirtualFile("helloWorld.ts", contentTs);
        this.openInPreview = virtualFile;
        Document document = editorFactory.createDocument(contentTs);
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor e =  editorFactory.createEditor(document, project, virtualFile, true, EditorKind.MAIN_EDITOR);
        customizeEditorSettings(e.getSettings());
        editorPanel.add(e.getComponent(), BorderLayout.CENTER);
        editorPanel.invalidate();
        editorPanel.validate();
//        LogicalPosition pos = e.offsetToLogicalPosition(result.getOffsetAndLength().getOffset());
        LogicalPosition pos = new LogicalPosition(0, 5);
//        Point point = e.logicalPositionToXY(pos);

//        e.getScrollingModel().scrollTo(pos, ScrollType.CENTER);
//        e.getScrollingModel().getVisibleArea().setLocation(point);

        e.getCaretModel().moveToVisualPosition(e.logicalToVisualPosition(pos));
        e.getScrollingModel().scrollToCaret(ScrollType.CENTER);

//        TextAttributes attributes = new TextAttributes();
//        attributes.setBackgroundColor(JBColor.BLUE);
        TextAttributes attributes = new TextAttributes(null, JBColor.YELLOW, JBColor.YELLOW, EffectType.BOXED, Font.PLAIN);
        e.getColorsScheme().getColor(EditorColors.CARET_COLOR);
//        SimpleTextAttributes attributes = new SimpleTextAttributes(null, JBColor.YELLOW, JBColor.BLUE,
//                ~SimpleTextAttributes.STYLE_BOLD |
//                        SimpleTextAttributes.STYLE_SEARCH_MATCH);

        LogicalPosition end = new LogicalPosition(0, 8);
        System.out.println(pos);
        System.out.println(end);

        HighlightManager highlightManager = HighlightManager.getInstance(project);
        highlightManager.addOccurrenceHighlight(e, e.logicalPositionToOffset(pos), e.logicalPositionToOffset(end), attributes, 0, null, null);

        e.getScrollingModel().addVisibleAreaListener(e1 -> {
            // I have no idea why, but if you don't have this listener the scrolling just doesn't work.
        });
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
        this.window.getBrowser().getCefBrowser().executeJavaScript("window.__sgfocus();", "", 0);

        this.window.getBrowser().openDevtools();
        //        CefBrowser myDevTools = this.webView.getCefBrowser().getDevTools();
//        JBCefBrowser myDevToolsBrowser = new JBCefBrowser(myDevTools,
//                this.webView.getJBCefClient());
//
//        myDevToolsBrowser.openDevtools();



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

            ShortcutSet mnemonicAsShortcut = ActionUtil.getMnemonicAsShortcut(this);
//            if (mnemonicAsShortcut != null) {
//                System.out.println(mnemonicAsShortcut);
//                setShortcutSet(mnemonicAsShortcut);
//                registerCustomShortcutSet(mnemonicAsShortcut, searchTextArea);
//            }
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

//            if (StringUtils.isNotEmpty(searchTextArea.getTextArea().getText())) {
//                doSearch("");
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