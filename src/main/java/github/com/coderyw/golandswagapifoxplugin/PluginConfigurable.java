package github.com.coderyw.golandswagapifoxplugin;


import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class PluginConfigurable implements Configurable {
    private final Project project;
    private PluginSettingsForm settingsForm;
    public PluginConfigurable(Project project) {
        this.project = project;
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "My Plugin Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        settingsForm = new PluginSettingsForm();
        return settingsForm.getRootPanel();
    }

    @Override
    public boolean isModified() {
        PluginSettings settings = project.getService(PluginSettings.class);
        return !settingsForm.getApiUrl().equals(settings.apiUrl) ||
                !settingsForm.getApiKey().equals(settings.apiKey) ||
                !settingsForm.getProjectId().equals(settings.projectId) ||
                !settingsForm.getParentFolderId().equals(settings.parentFolderId) ||
                !settingsForm.getMergeSelectedOption().equals(settings.mergeSelectedOption)||
                !settingsForm.isAddBasePath() == settings.isAddBasePath;
    }

    @Override
    public void apply() {
        PluginSettings settings = project.getService(PluginSettings.class);
        settings.apiUrl = settingsForm.getApiUrl();
        settings.apiKey = settingsForm.getApiKey();
        settings.projectId = settingsForm.getProjectId();
        settings.parentFolderId = settingsForm.getParentFolderId();
        settings.mergeSelectedOption = settingsForm.getMergeSelectedOption();
        settings.isAddBasePath = settingsForm.isAddBasePath();
    }

    @Override
    public void reset() {
        PluginSettings settings = project.getService(PluginSettings.class);
        settingsForm.setApiUrl(settings.apiUrl);
        settingsForm.setApiKey(settings.apiKey);
        settingsForm.setProjectId(settings.projectId);
        settingsForm.setParentFolderId(settings.parentFolderId);
        settingsForm.setSMergeSelectedOption(settings.mergeSelectedOption);
        settingsForm.setAddBasePath(settings.isAddBasePath);
    }
}