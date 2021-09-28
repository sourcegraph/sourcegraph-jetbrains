package hackathon;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import org.apache.commons.collections.ListUtils;
import org.fest.util.Collections;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class SearchResultsTreeView implements Disposable {
    private final JPanel panel;
    private final Tree tree;
    private TreeSpeedSearch treeSpeedSearch;

    public SearchResultsTreeView(List<SearchResult> results) {
        System.out.println(results.size());
        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(JBUI.size(800, 800));

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Match Type");
        TreeModel model = new DefaultTreeModel(root);

        Map<String, Collection<SearchResult>> byType = groupResultsByType(results).asMap();
        DefaultMutableTreeNode repoTypeMatch = new DefaultMutableTreeNode("Repository Match: " + byType.get("repository").size());
        root.add(repoTypeMatch);
        for (SearchResult repositoryMatch : byType.get("repository")) {
            DefaultMutableTreeNode repo = new DefaultMutableTreeNode(repositoryMatch.getRepo());
            repoTypeMatch.add(repo);
        }

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
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.preview);
                    fileNode.add(childNode);
                }
            });

            fileTypeMatch.add(repoNode);
        });
        tree = new Tree(model);
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(tree);
        panel.add(scrollPane);

        treeSpeedSearch = new TreeSpeedSearch(tree);
    }

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
    }
}
