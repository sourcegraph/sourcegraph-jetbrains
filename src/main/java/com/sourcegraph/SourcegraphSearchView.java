package com.sourcegraph;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.ui.OnePixelSplitter;
import com.intellij.ui.PopupBorder;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

public class SourcegraphSearchView implements Disposable {
    private final JPanel panel;
    private boolean isDisposed = false;
    private final EditorFactory editorFactory;

    private static final KeyStroke F4 = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);

    public SourcegraphSearchView(Project project) {
        /* Create panel */
        panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(JBUI.size(1200, 800));
        panel.setBorder(PopupBorder.Factory.create(true, true));

        /* Create top and bottom parts, with a splitter between them */
        JPanel topPanel = new JPanel(new BorderLayout());
        Splitter splitter = new OnePixelSplitter(true, 0.5f, 0.1f, 0.9f);
        JPanel previewPanel = new JPanel(new BorderLayout());
        splitter.setFirstComponent(topPanel);
        splitter.setSecondComponent(previewPanel);
        panel.add(splitter, BorderLayout.CENTER);

        /* Create virtual files */
        LightVirtualFile virtualTSFile = new LightVirtualFile("helloWorld.ts", "let message: string = 'Hello, TypeScript 2!';\n" +
                "\n" +
                "let heading = document.createElement('h1');\n" +
                "heading.textContent = message;\n" +
                "\n" +
                "document.body.appendChild(heading);");
        LightVirtualFile virtualJavaFile = new LightVirtualFile("helloWorld.java", "import com.intellij.ui.components.SomethingThatIsMadeUp;\n" +
                "\n" +
                "class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        SomethingThatIsMadeUp.test();\n" +
                "        System.out.println(\"Hello, World!\"); \n" +
                "    }\n" +
                "}");

        /* Create editor */
        JBPanel<JBPanelWithEmptyText> editorPanel1 = new JBPanelWithEmptyText(new BorderLayout());
        JBPanel<JBPanelWithEmptyText> editorPanel2 = new JBPanelWithEmptyText(new BorderLayout());
        previewPanel.add(editorPanel1, BorderLayout.NORTH);
        previewPanel.add(editorPanel2, BorderLayout.SOUTH);

        ActionListener openTSInEditorAction = actionEvent -> FileEditorManager.getInstance(project).openTextEditor(
                new OpenFileDescriptor(project, virtualTSFile, 0), true);
        ActionListener openJavaInEditorAction = actionEvent -> FileEditorManager.getInstance(project).openTextEditor(
                new OpenFileDescriptor(project, virtualJavaFile, 0), true);

        editorFactory = EditorFactory.getInstance();
        Editor editor1 = createEditor(virtualTSFile, true);
        Editor editor2 = createEditor(virtualJavaFile, false);
        editorPanel1.add(editor1.getComponent(), BorderLayout.NORTH);
        editorPanel2.add(editor2.getComponent(), BorderLayout.SOUTH);
        editorPanel1.registerKeyboardAction(openTSInEditorAction, F4, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        editorPanel2.registerKeyboardAction(openJavaInEditorAction, F4, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    public boolean isDisposed() {
        return isDisposed;
    }


    @NotNull
    private Editor createEditor(LightVirtualFile virtualFile, boolean readOnly) {
        Document document = editorFactory.createDocument(virtualFile.getContent());
        Project project = ProjectManager.getInstance().getOpenProjects()[0];
        Editor editor = editorFactory.createEditor(document, project, virtualFile, readOnly, EditorKind.MAIN_EDITOR);
        EditorSettings settings = editor.getSettings();
        settings.setLineMarkerAreaShown(true);
        settings.setFoldingOutlineShown(false);
        settings.setAdditionalColumnsCount(0);
        settings.setAdditionalLinesCount(0);
        settings.setAnimatedScrolling(false);
        settings.setAutoCodeFoldingEnabled(false);
        return editor;
    }

    public JBPopup createPopup() {
        return JBPopupFactory.getInstance().createComponentPopupBuilder(panel, panel)
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
    }

    @Override
    public void dispose() {
        isDisposed = true;
    }
}
