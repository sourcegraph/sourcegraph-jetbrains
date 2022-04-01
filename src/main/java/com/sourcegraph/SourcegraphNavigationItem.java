package com.sourcegraph;

import com.intellij.ide.util.PsiNavigationSupport;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public class SourcegraphNavigationItem implements NavigationItem {
    private final @NotNull Navigatable myNavigatable;
    private final @NotNull String myName;
    private final @NotNull VirtualFile myFile;
    private final int myPosition;

    public SourcegraphNavigationItem(@NotNull Project project, @NotNull String name, @NotNull VirtualFile file, int position) {
        myNavigatable = PsiNavigationSupport.getInstance().createNavigatable(project, file, position);
        myName = name;
        myFile = file;
        myPosition = position;
    }

    @Override
    public void navigate(boolean requestFocus) {
        myNavigatable.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @NotNull
    @Override
    public String getName() {
        return myName;
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @NotNull
            @Override
            public String getPresentableText() {
                return myName;
            }

            @NotNull
            @Override
            public String getLocationString() {
                return myFile.toString();
            }

            @NotNull
            @Override
            public Icon getIcon(boolean unused) {
                return IconLoader.getIcon("/icons/icon.png", SourcegraphNavigationItem.class);
            }
        };
    }
}