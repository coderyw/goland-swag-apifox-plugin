package github.com.coderyw.golandswagapifoxplugin;


import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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
        if (settings == null ) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("Setting get error!", "Error");
            });
            return false;
        }
        return !settingsForm.getApiUrl().equals(settings.apiUrl) ||
                !settingsForm.getApiKey().equals(settings.apiKey) ||
                !settingsForm.getProjectId().equals(settings.projectId) ||
                !settingsForm.getParentFolderId().equals(settings.parentFolderId) ||
                !settingsForm.getMergeSelectedOption().equals(settings.mergeSelectedOption)||
                !settingsForm.isAddBasePath() == settings.isAddBasePath||
                !settingsForm.getGopath().equals(settings.goPath)||
                !settingsForm.getGoRoot().equals(settings.goRoot);
    }

    @Override
    public void apply() {
        PluginSettings settings = project.getService(PluginSettings.class);
        if (settings == null ) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("Setting get error!", "Error");
            });
            return;
        }
        settings.apiUrl = settingsForm.getApiUrl();
        settings.apiKey = settingsForm.getApiKey();
        settings.projectId = settingsForm.getProjectId();
        settings.parentFolderId = settingsForm.getParentFolderId();
        settings.mergeSelectedOption = settingsForm.getMergeSelectedOption();
        settings.isAddBasePath = settingsForm.isAddBasePath();
        settings.goPath = settingsForm.getGopath();
        settings.goRoot = settingsForm.getGoRoot();
    }

    @Override
    public void reset() {
        PluginSettings settings = project.getService(PluginSettings.class);
        if (settings == null ) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("Setting get error!", "Error");
            });
            return;
        }
        settingsForm.setApiUrl(settings.apiUrl);
        settingsForm.setApiKey(settings.apiKey);
        settingsForm.setProjectId(settings.projectId);
        settingsForm.setParentFolderId(settings.parentFolderId);
        settingsForm.setSMergeSelectedOption(settings.mergeSelectedOption);
        settingsForm.setAddBasePath(settings.isAddBasePath);
        settingsForm.setGopath(settings.goPath);
        settingsForm.setGoRoot(settings.goRoot);
    }
}