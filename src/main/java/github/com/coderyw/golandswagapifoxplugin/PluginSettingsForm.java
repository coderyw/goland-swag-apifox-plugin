package github.com.coderyw.golandswagapifoxplugin;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.*;


import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class PluginSettingsForm {
    private JBPanel rootPanel;
    private JBTextField apiUrlField;
    private JBTextField apiKeyField;
    private JBTextField projectIdField;
    private JBTextField parentFolderIdField;
    private JComboBox<String> mergeComboBox;
    private JBCheckBox addBasePathCheckBox;

//    private TextFieldWithBrowseButton filePathField;

    public PluginSettingsForm() {
        rootPanel = new JBPanel(new GridLayout(15, 2, 1, 1)); // 使用网格布局
        rootPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 添加内边距
        // API URL 输入框
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS)); // 水平排列
        p1.add(new JBLabel("API URL:"));
        p1.add(Box.createHorizontalStrut(5)); // 添加间距
        apiUrlField = new JBTextField();
        apiUrlField.setEditable(false);
        p1.add(apiUrlField);
        rootPanel.add(p1);
//        apiUrlField.setPreferredSize(new Dimension(2, 1)); // 设置宽度为 200



        // API Key 输入框
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS)); // 水平排列
        p2.add(new JBLabel("API Key:"));
        p2.add(Box.createHorizontalStrut(5)); // 添加间距
        apiKeyField = new JBTextField();
        p2.add(apiKeyField);
        rootPanel.add(p2);

        // 项目id 输入框
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS)); // 水平排列
        p3.add(new JBLabel("Project ID:"));
        p3.add(Box.createHorizontalStrut(5)); // 添加间距
        projectIdField = new JBTextField();
        p3.add(projectIdField);
        rootPanel.add(p3);

        // 父级目录id 输入框
        JPanel p4 = new JPanel();
        p4.setLayout(new BoxLayout(p4, BoxLayout.X_AXIS)); // 水平排列
        p4.add(new JBLabel("Parent folder ID:"));
        p4.add(Box.createHorizontalStrut(5)); // 添加间距
        parentFolderIdField = new JBTextField();
        p4.add(parentFolderIdField);
        rootPanel.add(p4);

        // 处理接口匹配行为
        JPanel p5 = new JPanel();
        p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS)); // 水平排列
        p5.add(new JBLabel("Endpoint overwrite behavior:"));
        p5.add(Box.createHorizontalStrut(5)); // 添加间距
        mergeComboBox = new ComboBox<>(new String[]{"Overwrite existing", "Auto merge", "Keep existing", "Create new"});
        mergeComboBox.setSelectedIndex(1);
        p5.add(mergeComboBox);
        rootPanel.add(p5);

        // 是否添加basePath
        JPanel p6 = new JPanel();
        p6.setLayout(new BoxLayout(p6, BoxLayout.X_AXIS)); // 水平排列
        p6.add(new JBLabel("Add base path:"));
        p6.add(Box.createHorizontalStrut(5)); // 添加间距
        addBasePathCheckBox = new JBCheckBox();
        p6.add(addBasePathCheckBox);
        rootPanel.add(p6);


    }

    public boolean isAddBasePath() {
        return addBasePathCheckBox.isSelected();
    }

    public void setAddBasePath(boolean selected) {
         addBasePathCheckBox.setSelected(selected);
    }

    public String getMergeSelectedOption() {
        String item = (String) mergeComboBox.getSelectedItem();
        switch (Objects.requireNonNull(item)){
            case "Overwrite existing":
                return "OVERWRITE_EXISTING";
                case "Auto merge":
                    return "AUTO_MERGE";
                case "Keep existing":
                    return "KEEP_EXISTING";
                case "Create new":
                    return "CREATE_NEW";

        }

        return "";
    }

    public void setSMergeSelectedOption(String option) {
        switch (option){
            case "OVERWRITE_EXISTING":
                mergeComboBox.setSelectedIndex(0);
                return;
                case "AUTO_MERGE":
                    mergeComboBox.setSelectedIndex(1);
                    return;
                case "KEEP_EXISTING":
                    mergeComboBox.setSelectedIndex(2);
                    return;
                case "CREATE_NEW":
                    mergeComboBox.setSelectedIndex(3);
                    return;
        }
    }

    public String getProjectId() {
        return projectIdField.getText();
    }

    public String getParentFolderId() {
        return parentFolderIdField.getText();
    }


    public void setProjectId(String projectId) {
        projectIdField.setText(projectId);
    }

    public void setParentFolderId(String parentFolderId) {
        parentFolderIdField.setText(parentFolderId);
    }



    public JComponent getRootPanel() {
        return rootPanel;
    }

    public String getApiUrl() {
        return apiUrlField.getText();
    }

    public void setApiUrl(String apiUrl) {
        apiUrlField.setText(apiUrl);
    }

    public String getApiKey() {
        return apiKeyField.getText();
    }

    public void setApiKey(String apiKey) {
        apiKeyField.setText(apiKey);
    }


}