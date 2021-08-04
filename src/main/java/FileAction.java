import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

public abstract class FileAction extends AnAction {

    abstract void handleFileUri(String uri);

    @Override
    public void actionPerformed(AnActionEvent e) {
        Logger logger = Logger.getInstance(this.getClass());

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
        if (currentDoc == null) {
            return;
        }
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        if (currentFile == null) {
            return;
        }
        SelectionModel sel = editor.getSelectionModel();

        // Get repo information.
        RepoInfo repoInfo = Util.repoInfo(currentFile.getPath());
        if (repoInfo.remoteURL == "") {
            return;
        }

        // Build the URL that we will open.
        String productName = ApplicationInfo.getInstance().getVersionName();
        String productVersion = ApplicationInfo.getInstance().getFullVersion();
        String uri;
        // set defaultBranch if config option is not null
        String branch = Util.setDefaultBranch(project)!=null ? Util.setDefaultBranch(project) : repoInfo.branch;
        String remoteURL = repoInfo.remoteURL;

        // replace remoteURL if config option is not null
        if(Util.setRemoteUrlReplacements(project)!=null) {
            String r = Util.setRemoteUrlReplacements(project);
            String[] replacements = r.trim().split("\\s*,\\s*");
            // Check if the entered values are pairs with no more than 2 pairs
             for (int i = 0; i < replacements.length && replacements.length % 2 == 0; i += 2) {
                remoteURL = remoteURL.replace(replacements[i], replacements[i+1]);
            }
        }

        try {
            LogicalPosition start = editor.visualToLogicalPosition(sel.getSelectionStartPosition());
            LogicalPosition end = editor.visualToLogicalPosition(sel.getSelectionEndPosition());
            uri = Util.sourcegraphURL(project)+"-/editor"
                    + "?remote_url=" + URLEncoder.encode(remoteURL, "UTF-8")
                    + "&branch=" + URLEncoder.encode(branch, "UTF-8")
                    + "&file=" + URLEncoder.encode(repoInfo.fileRel, "UTF-8")
                    + "&editor=" + URLEncoder.encode("JetBrains", "UTF-8")
                    + "&version=" + URLEncoder.encode(Util.VERSION, "UTF-8")
                    + "&utm_product_name=" + URLEncoder.encode(productName, "UTF-8")
                    + "&utm_product_version=" + URLEncoder.encode(productVersion, "UTF-8")
                    + "&start_row=" + URLEncoder.encode(Integer.toString(start.line), "UTF-8")
                    + "&start_col=" + URLEncoder.encode(Integer.toString(start.column), "UTF-8")
                    + "&end_row=" + URLEncoder.encode(Integer.toString(end.line), "UTF-8")
                    + "&end_col=" + URLEncoder.encode(Integer.toString(end.column), "UTF-8");
        } catch (UnsupportedEncodingException err) {
            logger.debug("failed to build URL");
            err.printStackTrace();
            return;
        }

        handleFileUri(uri);
    }
}
