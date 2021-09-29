package hackathon.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "hackathon.settings.SourcegraphLocationSettingsState",
        storages = {@Storage("SourcegraphLocations.xml")}
)
public class SourcegraphLocationSettingsState implements PersistentStateComponent<SourcegraphLocationSettingsState> {


    @Override
    public @Nullable SourcegraphLocationSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SourcegraphLocationSettingsState state) {
        XmlSerializerUtil.copyBean(state, this);

    }
}
