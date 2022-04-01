// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.sourcegraph;

import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributor;
import com.intellij.ide.actions.searcheverywhere.SearchEverywhereContributorFactory;
import com.intellij.ide.util.NavigationItemListCellRenderer;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.project.Project;
import com.intellij.testFramework.LightVirtualFile;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SourcegraphSearchEverywhereContributor implements SearchEverywhereContributor<SourcegraphNavigationItem> {
    private final Project myProject;

    public SourcegraphSearchEverywhereContributor(Project project) {
        myProject = project;
    }
    @NotNull
    @Override
    public String getSearchProviderId() {
        return getClass().getSimpleName();
    }

    @NotNull
    @Override
    public String getGroupName() {
        return "Sourcegraph++++";
    }

    @NotNull
    @Override
    public String getFullGroupName() {
        return "Sourcegraph++++";
    }

    @Override
    public int getSortWeight() {
        // lets show yaml keys in the last order
        // all other implementations have weight from 50 to 300
        return 1;
    }

    @Override
    public boolean isShownInSeparateTab() {
        return true;
    }

    @Override
    public boolean showInFindResults() {
        return false;
    }

    @Override
    public void fetchElements(@NotNull String pattern,
                              @NotNull ProgressIndicator progressIndicator,
                              @NotNull Processor<? super SourcegraphNavigationItem> consumer) {
        if (myProject == null || pattern.isEmpty()) {
            return;
        }

        Runnable task = () -> findKeys(consumer, pattern, progressIndicator);
        Application application = ApplicationManager.getApplication();
        if (application.isUnitTestMode()) {
            application.runReadAction(task);
        } else {
            if (application.isDispatchThread())
                throw new IllegalStateException("This method must not be called from EDT");
            ProgressIndicatorUtils.yieldToPendingWriteActions();
            ProgressIndicatorUtils.runInReadActionWithWriteActionPriority(task, progressIndicator);
        }
    }


    @Override
    public boolean processSelectedItem(@NotNull SourcegraphNavigationItem selected, int modifiers, @NotNull String searchText) {
        return true;
    }

    @NotNull
    @Override
    public ListCellRenderer<? super Object> getElementsRenderer() {
        return new NavigationItemListCellRenderer();
    }

    @Override
    public Object getDataForItem(@NotNull SourcegraphNavigationItem element, @NotNull String dataId) {
        return null;
    }

    private void findKeys(@NotNull Processor<? super SourcegraphNavigationItem> consumer,
                          @NotNull String pattern,
                          ProgressIndicator progressIndicator) {
        if (ActionUtil.isDumbMode(myProject)) {
            return;
        }
        assert myProject != null;

        consumer.process(new SourcegraphNavigationItem(myProject, "Result 1", new LightVirtualFile("Test.jsx", "hello i am a java"), 1));
        consumer.process(new SourcegraphNavigationItem(myProject, "Result 2", new LightVirtualFile("Test.jsx", "hello i am a java 2"), 1));
    }


    public static class Factory implements SearchEverywhereContributorFactory<SourcegraphNavigationItem> {
        @NotNull
        @Override
        public SearchEverywhereContributor<SourcegraphNavigationItem> createContributor(@NotNull AnActionEvent initEvent) {
            return new SourcegraphSearchEverywhereContributor(initEvent.getProject());
        }
    }
}