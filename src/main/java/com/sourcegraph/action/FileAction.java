package com.sourcegraph.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.sourcegraph.project.RepoInfo;
import com.sourcegraph.util.SourcegraphUtil;

<<<<<<< HEAD:src/main/java/FileAction.java
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
=======
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

>>>>>>> origin/master:src/main/java/com/sourcegraph/action/FileAction.java

public abstract class FileAction extends AnAction {

    abstract void handleFileUri(String uri);

    @Override
    public void actionPerformed(AnActionEvent e) {
        // Get project, editor, document, file, and position information.
        final Project project = e.getProject();
        if (project == null) {
            return;
        }
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }
        Document currentDoc = editor.getDocument();
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        if (currentFile == null) {
            return;
        }
        SelectionModel sel = editor.getSelectionModel();

        // Get repo information.
<<<<<<< HEAD:src/main/java/FileAction.java
        RepoInfo repoInfo = Util.repoInfo(currentFile.getPath(), project);
        if (Objects.equals(repoInfo.remoteURL, "")) {
=======
        RepoInfo repoInfo = SourcegraphUtil.repoInfo(currentFile.getPath(), project);
        if (repoInfo.remoteURL == "") {
>>>>>>> origin/master:src/main/java/com/sourcegraph/action/FileAction.java
            return;
        }

        // Build the URL that we will open.
        String productName = ApplicationInfo.getInstance().getVersionName();
        String productVersion = ApplicationInfo.getInstance().getFullVersion();
        String uri;

<<<<<<< HEAD:src/main/java/FileAction.java
        VisualPosition selectionStartPosition = sel.getSelectionStartPosition();
        VisualPosition selectionEndPosition = sel.getSelectionEndPosition();
        LogicalPosition start = selectionStartPosition != null ? editor.visualToLogicalPosition(selectionStartPosition) : null;
        LogicalPosition end = selectionEndPosition != null ? editor.visualToLogicalPosition(selectionEndPosition) : null;
        uri = Util.sourcegraphURL(project)+"-/editor"
                + "?remote_url=" + URLEncoder.encode(repoInfo.remoteURL, StandardCharsets.UTF_8)
                + "&branch=" + URLEncoder.encode(repoInfo.branch, StandardCharsets.UTF_8)
                + "&file=" + URLEncoder.encode(repoInfo.fileRel, StandardCharsets.UTF_8)
                + "&editor=" + URLEncoder.encode("JetBrains", StandardCharsets.UTF_8)
                + "&version=" + URLEncoder.encode(Util.VERSION, StandardCharsets.UTF_8)
                + (start != null ? ("&start_row=" + URLEncoder.encode(Integer.toString(start.line), StandardCharsets.UTF_8)
                + "&start_col=" + URLEncoder.encode(Integer.toString(start.column), StandardCharsets.UTF_8)) : "")
                + (end != null ? ("&end_row=" + URLEncoder.encode(Integer.toString(end.line), StandardCharsets.UTF_8)
                + "&end_col=" + URLEncoder.encode(Integer.toString(end.column), StandardCharsets.UTF_8)) : "")
                + "&utm_product_name=" + URLEncoder.encode(productName, StandardCharsets.UTF_8)
                + "&utm_product_version=" + URLEncoder.encode(productVersion, StandardCharsets.UTF_8);
=======
        try {
            LogicalPosition start = editor.visualToLogicalPosition(sel.getSelectionStartPosition());
            LogicalPosition end = editor.visualToLogicalPosition(sel.getSelectionEndPosition());
            uri = SourcegraphUtil.sourcegraphURL(project)+"-/editor"
                    + "?remote_url=" + URLEncoder.encode(repoInfo.remoteURL, "UTF-8")
                    + "&branch=" + URLEncoder.encode(repoInfo.branch, "UTF-8")
                    + "&file=" + URLEncoder.encode(repoInfo.fileRel, "UTF-8")
                    + "&editor=" + URLEncoder.encode("JetBrains", "UTF-8")
                    + "&version=" + URLEncoder.encode(SourcegraphUtil.VERSION, "UTF-8")
                    + "&start_row=" + URLEncoder.encode(Integer.toString(start.line), "UTF-8")
                    + "&start_col=" + URLEncoder.encode(Integer.toString(start.column), "UTF-8")
                    + "&end_row=" + URLEncoder.encode(Integer.toString(end.line), "UTF-8")
                    + "&end_col=" + URLEncoder.encode(Integer.toString(end.column), "UTF-8")
                    + "&utm_product_name=" + URLEncoder.encode(productName, "UTF-8")
                    + "&utm_product_version=" + URLEncoder.encode(productVersion, "UTF-8");
        } catch (UnsupportedEncodingException err) {
            logger.debug("failed to build URL");
            err.printStackTrace();
            return;
        }
>>>>>>> origin/master:src/main/java/com/sourcegraph/action/FileAction.java

        handleFileUri(uri);
    }
}
