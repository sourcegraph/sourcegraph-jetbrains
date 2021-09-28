package hackathon;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.AppUIExecutor;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.fest.util.Collections;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.nio.file.spi.FileTypeDetector;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SearchResultsTreeView implements Disposable {
    private final JPanel panel;
    private final Tree tree;
    private TreeSpeedSearch treeSpeedSearch;
    private boolean isDisposed = false;
    private EditorFactory editorFactory;
//    private Editor editor;

//    private Logger logger = new L

    public boolean isDisposed() {
        return isDisposed;
    }

    public SearchResultsTreeView(List<SearchResult> results) {
        System.out.println(results.size());
        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(JBUI.size(1200, 800));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Match Type");
        TreeModel model = new DefaultTreeModel(root);

        Map<String, Collection<SearchResult>> byType = groupResultsByType(results).asMap();

        if (CollectionUtils.isNotEmpty(byType.get("repository"))) {
            DefaultMutableTreeNode repoTypeMatch = new DefaultMutableTreeNode("Repository Match: " + byType.get("repository").size());
            root.add(repoTypeMatch);
            for (SearchResult repositoryMatch : byType.get("repository")) {
                SearchResultTreeNode repo = new SearchResultTreeNode(repositoryMatch.getRepo(), repositoryMatch);
                repoTypeMatch.add(repo);
            }
        }

        if (CollectionUtils.isNotEmpty(byType.get("file"))) {
            Map<String, Collection<SearchResult>> byRepo = groupResultsByRepo(byType.get("file")).asMap();

            DefaultMutableTreeNode fileTypeMatch = new DefaultMutableTreeNode("File Match: " + byType.get("file").size());
            root.add(fileTypeMatch);
            byRepo.forEach((key, sr) -> {
                DefaultMutableTreeNode repoNode = new DefaultMutableTreeNode(key);
                if (Collections.isNullOrEmpty(sr)) {
                    return;
                }

                groupResultsByFile(sr).asMap().forEach((fileKey, matches) -> {
                    DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileKey);
                    repoNode.add(fileNode);
                    for (SearchResult child : matches) {
                        SearchResultTreeNode childNode = new SearchResultTreeNode(child.preview, child);
                        fileNode.add(childNode);
                    }
                });

                fileTypeMatch.add(repoNode);
            });
        }

        tree = new Tree(model);
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(tree);
//        panel.add(scrollPane);
//        panel.add(previewPanel());

        Splitter splitter = new OnePixelSplitter(false, 0.5f, 0.1f, 0.9f);
        splitter.setFirstComponent(scrollPane);

        editorFactory = EditorFactory.getInstance();
//        Document document = editorFactory.createDocument("test 1234567");
//        Editor editor = editorFactory.createViewer(document);

        Editor defaultEditor = editorFactory.createViewer(editorFactory.createDocument(""));

        JBPanelWithEmptyText jbPanelWithEmptyText = new JBPanelWithEmptyText(new BorderLayout());
        jbPanelWithEmptyText.add(defaultEditor.getComponent(), BorderLayout.CENTER);

        splitter.setSecondComponent(jbPanelWithEmptyText);
        panel.add(splitter);

        treeSpeedSearch = new TreeSpeedSearch(tree);

        tree.getSelectionModel().addTreeSelectionListener(event -> {
            SwingUtilities.invokeLater(() -> {
                if (!isDisposed()) {
//                    updateOnSelectionChanged();
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node instanceof SearchResultTreeNode) {
                        System.out.println("updating editor");
                        // do a thing here
                        SearchResultTreeNode srn = (SearchResultTreeNode) node;
                        AppUIExecutor.onUiThread().execute(() -> {
                            jbPanelWithEmptyText.removeAll();
                            editorFromSearchResult(jbPanelWithEmptyText, srn.getSearchResult());
                        });
                    }

//                    myNeedUpdateButtons = true;
                }
            });
        });
    }

    private void editorFromSearchResult(JPanel parent, SearchResult result) {
        System.out.println("search result to editor");
//        System.out.println(result);
//        VirtualFile virtualFile = new LightVirtualFile(result.getFile(), result.getContent());
        Document document = editorFactory.createDocument(result.getContent());
        Editor e =  editorFactory.createEditor(document, ProjectManager.getInstance().getOpenProjects()[0], EditorKind.PREVIEW);
        customizeEditorSettings(e.getSettings());
        parent.add(e.getComponent(), BorderLayout.CENTER);
        parent.invalidate();
        parent.validate();
//        LogicalPosition pos = e.offsetToLogicalPosition(result.getOffsetAndLength().getOffset());
        LogicalPosition pos = new LogicalPosition(result.lineNumber, result.offsetAndLength.getOffset());
//        Point point = e.logicalPositionToXY(pos);

        System.out.println(result.lineNumber);
        System.out.println(result.offsetAndLength);
        System.out.println(pos);
//        e.getScrollingModel().scrollTo(pos, ScrollType.CENTER);
//        e.getScrollingModel().getVisibleArea().setLocation(point);

        e.getCaretModel().moveToVisualPosition(e.logicalToVisualPosition(pos));
        e.getScrollingModel().scrollToCaret(ScrollType.CENTER);

        e.getScrollingModel().addVisibleAreaListener(new VisibleAreaListener() {
            @Override
            public void visibleAreaChanged(@NotNull VisibleAreaEvent e) {
                // I have no idea why, but if you don't have this listener the scrolling just doesn't work.
            }
        });

//        return e;
    }

    private void customizeEditorSettings(EditorSettings settings) {
        settings.setLineMarkerAreaShown(true);
        settings.setFoldingOutlineShown(false);
        settings.setAdditionalColumnsCount(0);
        settings.setAdditionalLinesCount(0);
        settings.setAnimatedScrolling(false);
        settings.setAutoCodeFoldingEnabled(false);
    }

//    private void updateOnSelectionChanged(JPanel editorPanel) {
//        ApplicationManager.getApplication().assertIsDispatchThread();
//
//    }

//    private JPanel previewPanel(Editor editor) {
//        JPanel panel = new JPanel();
////        EditorTextField editorTextField = new EditorTextField("asdf");
////        panel.add(editorTextField);
//        panel.add(editor.getComponent(), BorderLayout.CENTER);
//        return panel;
//    }

    public JBPopup createPopup() {
        return JBPopupFactory.getInstance().createComponentPopupBuilder(panel, tree)
                .setTitle("Sourcegraph Search Results")
                .setResizable(true)
                .setModalContext(false)
                .setFocusable(true)
                .setRequestFocus(true)
                .setMovable(true)
                .setBelongsToGlobalPopupStack(true)
                .setCancelOnOtherWindowOpen(true)
                .setCancelKeyEnabled(false)
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

    @Override
    public void dispose() {
        isDisposed = true;
    }
}
