package hackathon;

import com.apollographql.apollo.exception.ApolloException;
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
import org.fest.util.Collections;
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

public class SearchResultsTreeView implements Disposable {

    private final Project project;

    private final JPanel panel;
    private final Tree tree;
    private boolean isDisposed = false;
    private DefaultMutableTreeNode root;
    private DefaultTreeModel model;
    private final EditorFactory editorFactory;
    private SearchTextArea searchTextArea;
    private SourcegraphSearchTextComponent sourcegraphSearchTextComponent;
    private List<SearchResult> mostRecentSearch;
    private Stack<String> queryHistory = new Stack<>();
    private ProjectManager projectManager = ProjectManager.getInstance();
    private JLabel matchCountLabel;
    private ImageIcon sourcegraphIcon;
    private SimpleColoredComponent pathInfoTitle;

    private JBPanel editorPanel;
    private JPanel titlePanel;

    private JLabel errorLabel;
    private JLabel loadingLabel;

    private AtomicBoolean caseSensitiveSelected = new AtomicBoolean(false);
    private AtomicBoolean regexSelected = new AtomicBoolean(false);
    private AtomicBoolean structuralSelected = new AtomicBoolean(false);

    private static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    private static final KeyStroke SHIFT_ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.SHIFT_DOWN_MASK);

    private SourcegraphClient sourcegraphClient;

    private VirtualFile openInPreview;

    private ExclusiveToggleActionGroup exclusiveToggleActionGroup;

    public SearchResultsTreeView(Project project) {
        this.project = project;
//        Logger focusLog = Logger.getLogger("java.awt.focus.Component");
//
//        // The logger should log all messages
//        focusLog.setLevel(Level.ALL);
//
//        // Create a new handler
//        ConsoleHandler handler = new ConsoleHandler();
//
//        // The handler must handle all messages
//        handler.setLevel(Level.ALL);
//
//        // Add the handler to the logger
//        focusLog.addHandler(handler);

        try {
            sourcegraphIcon = new ImageIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("/icons/icon.png")));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(JBUI.size(1200, 800));
        panel.setBorder(PopupBorder.Factory.create(true, true));

        root = new DefaultMutableTreeNode("Match Type");
        model = new DefaultTreeModel(root);

        tree = new Tree(model);

        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(tree);

        pathInfoTitle = new SimpleColoredComponent();
        pathInfoTitle.setBorder(JBUI.Borders.empty(3, 8, 4, 8));

        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.add(pathInfoTitle, BorderLayout.NORTH);

        Splitter splitter = new OnePixelSplitter(true, 0.5f, 0.1f, 0.9f);
        splitter.setFirstComponent(scrollPane);

        editorFactory = EditorFactory.getInstance();

        editorPanel = new JBPanelWithEmptyText(new BorderLayout());
        previewPanel.add(editorPanel);

        splitter.setSecondComponent(previewPanel);

        JBTextArea searchTextComponent = new JBTextArea();

        String[] keywords = new String[]{"repo:", "-repo:", "rev:", "repogroup:", "file:", "-file:", "content:", "-content:", "select:repo", "select:file", "select:content", "lang:", "-lang:", "type:", "case:yes", "fork:yes", "fork:only", "archived:yes", "archived:only", "count:", "count:all", "timeout:", "patternType:literal", "patternType:regexp", "patternType:structural", "visibility:any", "visibility:public", "visibility:private", "AND", "OR", "NOT"};

        TextFieldWithAutoCompletion<String> editorTextField = TextFieldWithAutoCompletion.create(project, Set.of(keywords), true, "");

//        EditorTextField editorTextField = new EditorTextField("test");
//        editorTextField.setOneLineMode(true);
//        editorTextField.addNotify();
//        editorTextField.getEditor().get;
        sourcegraphSearchTextComponent = new SourcegraphSearchTextComponent(editorTextField);

        searchTextArea = new SearchTextArea(searchTextComponent, true);
        searchTextComponent.setRows(1);
        searchTextArea.setBorder(new CompoundBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.BigPopup.searchFieldBorderColor(), 1, 0, 1, 0),
                JBUI.Borders.empty(1, 0, 2, 0)));

        sourcegraphSearchTextComponent.setBorder(new CompoundBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.BigPopup.searchFieldBorderColor(), 1, 0, 1, 0),
                JBUI.Borders.empty(1, 0, 2, 0)));

        ToggleAction[] toggleActions = new ToggleAction[]{
                new ToggleAction("Case Sensitive", "Use case sensitive search", AllIcons.Actions.MatchCase, AllIcons.Actions.MatchCaseHovered, AllIcons.Actions.MatchCaseSelected, caseSensitiveSelected),
                new ToggleAction("Regex Search", "Use regex search", AllIcons.Actions.Regex, AllIcons.Actions.RegexHovered, AllIcons.Actions.RegexSelected, regexSelected),
                new ToggleAction("Structural Search", "Use structural search", AllIcons.Actions.Words, AllIcons.Actions.WordsHovered, AllIcons.Actions.WordsSelected, structuralSelected)
        };
        // this isn't working - the idea was to disable the other actions like a buttongroup would, but for some reason it isn't!
//        exclusiveToggleActionGroup = new ExclusiveToggleActionGroup(List.of(toggleActions));

//        searchTextArea.setExtraActions(toggleActions);
        sourcegraphSearchTextComponent.setExtraActions(toggleActions);
//        ButtonGroup buttonGroup = new ButtonGroup();
//        UIUtil.findComponentsOfType(searchTextArea, ActionButton.class).forEach(b -> {
//            System.out.println("x");
//            buttonGroup.add(b.getRootPane().getDefaultButton());
//        });

        searchTextArea.setMultilineEnabled(false);
        titlePanel = new JPanel(new MigLayout("flowx, ins 0, gap 0, fillx, filly"));
        JLabel myTitleLabel = new JBLabel("Find on Sourcegraph", UIUtil.ComponentStyle.REGULAR);
        RelativeFont.BOLD.install(myTitleLabel);
        myTitleLabel.setIcon(sourcegraphIcon);
        matchCountLabel = new JBLabel("", UIUtil.ComponentStyle.SMALL);
        titlePanel.add(myTitleLabel, "gapright 4, gapleft 2");
        titlePanel.add(matchCountLabel);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        ComboBox<SourcegraphLocation> comboBox = new ComboBox<>(new SourcegraphLocation[]{new SourcegraphLocation("sourcegraph.com", "https://sourcegraph.com")});
        buttonPanel.add(comboBox, BorderLayout.EAST);
        buttonPanel.setBorder(new CompoundBorder(JBUI.Borders.customLine(JBUI.CurrentTheme.BigPopup.searchFieldBorderColor(), 0, 0, 0, 0),
                JBUI.Borders.empty(1, 0)));

        comboBox.addItemListener(event -> {
            if (event.getStateChange() == ItemEvent.SELECTED) {
                SourcegraphLocation location = (SourcegraphLocation) Objects.requireNonNull(comboBox.getSelectedItem());
                sourcegraphClient = new SourcegraphClient(location);
            }
        });

        // initialize client to default thing
        sourcegraphClient = new SourcegraphClient((SourcegraphLocation) Objects.requireNonNull(comboBox.getSelectedItem()));

        JPanel topPanel = new JPanel(new GridLayout(3, 1));
        topPanel.add(titlePanel);
//        topPanel.add(editorTextField);
        topPanel.add(sourcegraphSearchTextComponent);
//        topPanel.add(searchTextArea);
        JTextField jTextField = new JTextField();
        jTextField.setBorder(null);
//        topPanel.add(jTextField);
        topPanel.add(buttonPanel);


        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(splitter, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        // its magic
        TreeSpeedSearch treeSpeedSearch = new TreeSpeedSearch(tree);

        tree.getSelectionModel().addTreeSelectionListener(event -> SwingUtilities.invokeLater(() -> {
            if (!isDisposed()) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                if (node instanceof SearchResultTreeNode) {
                    System.out.println("updating editor");
                    SearchResultTreeNode srn = (SearchResultTreeNode) node;
                    AppUIExecutor.onUiThread().execute(() -> {
                        pathInfoTitle.clear();
                        if (StringUtils.isNotEmpty(srn.getSearchResult().getPath())) {
                            pathInfoTitle.append(srn.getSearchResult().getPath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                        }
                        editorPanel.removeAll();
                        editorFromSearchResult(srn.getSearchResult());
                    });
                }
            }
        }));

        errorLabel = new JLabel();
        errorLabel.setText("search error");
        errorLabel.setForeground(JBColor.RED);
        RelativeFont.BOLD.install(errorLabel);

        loadingLabel = new JLabel();
        titlePanel.add(loadingLabel, "w 24, wmin 24, gapleft 2");

        ActionListener searchAction = e -> {
            System.out.println("search action");
            doSearch(sourcegraphSearchTextComponent.getText());
        };

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
        sourcegraphSearchTextComponent.registerKeyboardAction(searchAction,KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        tree.registerKeyboardAction(openInSplitEditorAction, SHIFT_ENTER, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        sourcegraphSearchTextComponent.editorTextField().addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                sourcegraphSearchTextComponent.editorTextField().selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });

//        panel.setFocusTraversalPolicy(new ListFocusTraversalPolicy(List.of(searchTextComponent, tree)));
        panel.setFocusTraversalPolicy(new ListFocusTraversalPolicy(List.of(sourcegraphSearchTextComponent.editorTextField(), tree)));
        panel.setFocusCycleRoot(true);
    }

    public void doSearch(String query) {
        AppUIExecutor.onUiThread().execute(() -> {
            titlePanel.remove(errorLabel);
            titlePanel.revalidate();
            loadingLabel.setIcon(AnimatedIcon.Default.INSTANCE);

//            String query = searchTextArea.getTextArea().getText();
            System.out.println(query);
            queryHistory.push(query);

            if (!regexSelected.get() && !structuralSelected.get()) {
                sourcegraphClient.literalSearchAsync(query, caseSensitiveSelected.get(), this::onSearchSuccess, this::onSearchError);
            } else if (regexSelected.get()) {
                sourcegraphClient.regexSearchAsync(query, this::onSearchSuccess, this::onSearchError);
            } else if (structuralSelected.get()) {
                sourcegraphClient.structuralSearchAsync(query, this::onSearchSuccess, this::onSearchError);
            } else {
                throw new IllegalStateException("no idea how you managed this");
            }
        });
    }

    public boolean isDisposed() {
        return isDisposed;
    }

    private void clearResults() {
        if (root != null) {
            root.removeAllChildren();
        }
    }

    private void clearPreview() {
        SwingUtilities.invokeLater(() -> {
            openInPreview = null;
            editorPanel.removeAll();
            pathInfoTitle.clear();
            editorPanel.revalidate();
        });
    }

    private void handleSearch(List<SearchResult> results) {
        mostRecentSearch = results;
        clearResults();
        clearPreview();
        matchCountLabel.setText(String.format("%d matches", results.size()));
        if (CollectionUtils.isEmpty(results)) {
            model.reload();
            editorPanel.revalidate();
            return;
        }


        Map<String, Collection<SearchResult>> byType = groupResultsByType(results).asMap();

        if (CollectionUtils.isNotEmpty(byType.get("repository"))) {
            DefaultMutableTreeNode repoTypeMatch = new DefaultMutableTreeNode("Repository Match: " + byType.get("repository").size());
//            root.add(repoTypeMatch);
            for (SearchResult repositoryMatch : byType.get("repository")) {
                SearchResultTreeNode repo = new SearchResultTreeNode(repositoryMatch.getRepo(), repositoryMatch);
//                repoTypeMatch.add(repo);
                root.add(repo);
            }
        }
        if (CollectionUtils.isNotEmpty(byType.get("file"))) {
            Map<String, Collection<SearchResult>> byRepo = groupResultsByRepo(byType.get("file")).asMap();

            DefaultMutableTreeNode fileTypeMatch = new DefaultMutableTreeNode("File Match: " + byType.get("file").size());
//            root.add(fileTypeMatch);
            byRepo.forEach((key, sr) -> {
                DefaultMutableTreeNode repoNode = new DefaultMutableTreeNode(key);
                if (Collections.isNullOrEmpty(sr)) {
                    return;
                }

                groupResultsByFile(sr).asMap().forEach((fileKey, matches) -> {
                    DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileKey);
//                    repoNode.add(fileNode);
                    for (SearchResult child : matches) {
                        SearchResultTreeNode childNode = new SearchResultTreeNode(child.preview.trim(), child);
                        repoNode.add(childNode);
                    }
                });
//                fileTypeMatch.add(repoNode);
                root.add(repoNode);
            });
        }
        IdeFocusManager.getInstance(project).requestFocus(tree, true);
        tree.expandPath(tree.getPathForRow(0));

        model.reload();
    }

    private void editorFromSearchResult(SearchResult result) {
        System.out.println("search result to editor");
        VirtualFile virtualFile = new LightVirtualFile(result.getFile(), result.getContent());
        this.openInPreview = virtualFile;
        Document document = editorFactory.createDocument(result.getContent());
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor e =  editorFactory.createEditor(document, project, virtualFile, true, EditorKind.PREVIEW);
        customizeEditorSettings(e.getSettings());
        editorPanel.add(e.getComponent(), BorderLayout.CENTER);
        editorPanel.invalidate();
        editorPanel.validate();
//        LogicalPosition pos = e.offsetToLogicalPosition(result.getOffsetAndLength().getOffset());
        LogicalPosition pos = new LogicalPosition(result.lineNumber, result.offsetAndLength.getOffset());
//        Point point = e.logicalPositionToXY(pos);

        System.out.println(result.lineNumber);
        System.out.println(result.offsetAndLength);
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

        LogicalPosition end = new LogicalPosition(result.lineNumber, result.getOffsetAndLength().getOffset() + result.getOffsetAndLength().getLength());
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
        return JBPopupFactory.getInstance().createComponentPopupBuilder(panel, sourcegraphSearchTextComponent.editorTextField())
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

    private ImmutableListMultimap<String, SearchResult> groupResultsByType(Collection<SearchResult> results) {
        return Multimaps.index(results, searchResult -> searchResult.type);
    }

    private ImmutableListMultimap<String, SearchResult> groupResultsByRepo(Collection<SearchResult> results) {
        return Multimaps.index(results, searchResult -> searchResult.repo);
    }

    private ImmutableListMultimap<String, SearchResult> groupResultsByFile(Collection<SearchResult> results) {
        return Multimaps.index(results, searchResult -> searchResult.file != null ? searchResult.file : "");
    }

    public void show() {

    }

    @Override
    public void dispose() {
        isDisposed = true;
    }

    private void onSearchSuccess(List<SearchResult> r) {
        loadingLabel.setIcon(null);
        handleSearch(r);
    }

    private void onSearchError(ApolloException ex) {
        System.out.println(ex);
        loadingLabel.setIcon(null);
        titlePanel.add(errorLabel);
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
                registerCustomShortcutSet(mnemonicAsShortcut, searchTextArea);
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

            if (StringUtils.isNotEmpty(searchTextArea.getTextArea().getText())) {
                doSearch(sourcegraphSearchTextComponent.getText());
            }
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


