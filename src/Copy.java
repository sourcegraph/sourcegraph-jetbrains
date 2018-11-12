import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;

import java.io.*;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.net.URLEncoder;
import java.util.Optional;

public class Copy extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Logger logger = Logger.getInstance(this.getClass());

        // Get project, editor, document, file, and position information.
        final Project project = e.getProject();
        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }
        Optional<RepoInfo> oRepoInfo = Util.repoInfo(editor);
        if (!oRepoInfo.isPresent()) {
            return;
        }
        RepoInfo repoInfo = oRepoInfo.get();
        SelectionModel sel = editor.getSelectionModel();

        // Build the URL that we will open.
        String uri;
        String productName = ApplicationInfo.getInstance().getVersionName();
        String productVersion = ApplicationInfo.getInstance().getFullVersion();
        try {
            LogicalPosition start = editor.visualToLogicalPosition(sel.getSelectionStartPosition());
            LogicalPosition end = editor.visualToLogicalPosition(sel.getSelectionEndPosition());
            uri = Util.sourcegraphURL()+"-/editor"
                    + "?remote_url=" + URLEncoder.encode(repoInfo.remoteURL, "UTF-8")
                    + "&branch=" + URLEncoder.encode(repoInfo.revision, "UTF-8")
                    + "&file=" + URLEncoder.encode(repoInfo.fileRel, "UTF-8")
                    + "&start_row=" + URLEncoder.encode(Integer.toString(start.line), "UTF-8")
                    + "&start_col=" + URLEncoder.encode(Integer.toString(start.column), "UTF-8")
                    + "&end_row=" + URLEncoder.encode(Integer.toString(end.line), "UTF-8")
                    + "&end_col=" + URLEncoder.encode(Integer.toString(end.column), "UTF-8")
                    + "&editor=" + URLEncoder.encode("JetBrains", "UTF-8")
                    + "&version=" + URLEncoder.encode(Util.VERSION, "UTF-8")
                    + "&utm_product_name=" + URLEncoder.encode(productName, "UTF-8")
                    + "&utm_product_version=" + URLEncoder.encode(productVersion, "UTF-8");
        } catch (UnsupportedEncodingException err) {
            logger.debug("failed to build URL");
            err.printStackTrace();
            return;
        }



        // I can run the git --uno command to see if it's different.

        StringSelection selection = new StringSelection(uri);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }
}