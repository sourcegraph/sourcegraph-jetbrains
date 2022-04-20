package com.sourcegraph.project;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
  name = "Config",
  storages = {@Storage("sourcegraph.xml")})
public
class SourcegraphConfig implements PersistentStateComponent<SourcegraphConfig> {

    public String url;

    public String getUrl() {
        return url;
    }

    public String defaultBranch;

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public String remoteUrlReplacements;

    public String getRemoteUrlReplacements() {
        return remoteUrlReplacements;
    }

    @Nullable
    @Override
    public SourcegraphConfig getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SourcegraphConfig config) {
        this.url = config.url;
        this.defaultBranch = config.defaultBranch;
        this.remoteUrlReplacements = config.remoteUrlReplacements;
    }

<<<<<<< HEAD:src/main/java/Config.java
    static Config getInstance(Project project) {
        return project.getService(Config.class);
=======
    @Nullable
    public static SourcegraphConfig getInstance(Project project) {
        return ServiceManager.getService(project, SourcegraphConfig.class);
>>>>>>> origin/master:src/main/java/com/sourcegraph/project/SourcegraphConfig.java
    }
}
