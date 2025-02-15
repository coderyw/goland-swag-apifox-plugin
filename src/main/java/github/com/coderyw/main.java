package github.com.coderyw;

import github.com.coderyw.golandswagapifoxplugin.PluginSettingsForm;

import javax.swing.*;

public class main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Layout Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PluginSettingsForm form = new PluginSettingsForm();
        form.setApiUrl("https://api.example.com");
        form.setApiKey("test-key-123");

        frame.add(form.getRootPanel());
        frame.setSize(600, 400);
        frame.setVisible(true);
    }
}
