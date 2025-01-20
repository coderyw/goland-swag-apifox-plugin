package github.com.coderyw.golandswagapifoxplugin;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "PluginSettings", // 配置类的名称
        storages = @Storage("plugin-settings.xml") // 配置存储的文件名
)
public class PluginSettings implements PersistentStateComponent<PluginSettings> {
    public String apiUrl = "https://api.apifox.com/";
    public String apiKey = "";
    public String projectId = "";
    public String parentFolderId = "";
    public String mergeSelectedOption = "AUTO_MERGE";
    public boolean isAddBasePath = true;

    public String goPath = "";
    public String goRoot = "";

    @Nullable
    @Override
    public PluginSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull PluginSettings state) {
        this.apiUrl = state.apiUrl;
        this.apiKey = state.apiKey;
        this.projectId = state.projectId;
        this.parentFolderId = state.parentFolderId;
        this.mergeSelectedOption = state.mergeSelectedOption;
        this.isAddBasePath = state.isAddBasePath;
        this.goPath = state.goPath;
        this.goRoot = state.goRoot;
    }
}
