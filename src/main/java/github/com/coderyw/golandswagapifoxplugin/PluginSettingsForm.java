package github.com.coderyw.golandswagapifoxplugin;

import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.ui.components.*;
import com.intellij.openapi.application.ApplicationManager;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginSettingsForm {
    private JBPanel rootPanel;
    private JBTextField apiUrlField= new JBTextField();
    private JBTextField apiKeyField= new JBTextField();
    private JBTextField projectIdField= new JBTextField();
    private JBTextField parentFolderIdField= new JBTextField();
    private JComboBox<String> mergeComboBox=new JComboBox<>(new String[]{"Overwrite existing", "Auto merge", "Keep existing", "Create new"});
    private JBCheckBox addBasePathCheckBox=new JBCheckBox();
    private JBCheckBox swagPdCheckBox=new JBCheckBox();
    private JBTextField gopathField= new JBTextField();
    private JBTextField goRootField= new JBTextField();
    
    // 系统状态显示组件
    private JBLabel goStatusLabel = new JBLabel("Go: 检查中...");
    private JBLabel swagStatusLabel = new JBLabel("Swag: 检查中...");
    private JBLabel vmOptionsStatusLabel = new JBLabel("VM Options: 检查中...");
    private JButton checkSystemButton = new JButton("检查系统环境");
//    private TextFieldWithBrowseButton filePathField;

    // 修改主面板布局为左对齐流式布局
    private void buildUI() {
        rootPanel = new JBPanel(new BorderLayout());

        // 使用垂直盒子布局作为主容器
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // 系统检查分组
        mainPanel.add(createFullWidthGroup("系统环境检查",
                createSystemCheckPanel()
        ));

        mainPanel.add(Box.createVerticalStrut(15));

        // API配置分组
        mainPanel.add(createFullWidthGroup("Apifox配置",
                createStretchField("API URL:", apiUrlField),
                createStretchField("API Key:", apiKeyField),
                createStretchField("Project ID:", projectIdField),
                createStretchField("Parent folder ID:", parentFolderIdField),
                createFullWidthCombo("Endpoint overwrite behavior:", mergeComboBox)
        ));

        mainPanel.add(Box.createVerticalStrut(15));

        // 行为配置分组
        mainPanel.add(createFullWidthGroup("Swag配置",
                createCheckboxRow("Add base path: ", addBasePathCheckBox),
                createCheckboxRow("在依赖关系文件夹中进行解析，默认情况下禁用(默认：false): ", swagPdCheckBox)
        ));

        // 保证内容区域扩展
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        rootPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 初始化系统检查
        checkSystemEnvironment();
    }

    // 创建系统检查面板
    private JPanel createSystemCheckPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 状态显示区域
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.add(goStatusLabel);
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(swagStatusLabel);
        statusPanel.add(Box.createVerticalStrut(5));
        statusPanel.add(vmOptionsStatusLabel);
        
        // 检查按钮
        checkSystemButton.addActionListener(e -> checkSystemEnvironment());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(checkSystemButton);
        
        panel.add(statusPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    // 检查系统环境
    private void checkSystemEnvironment() {
        goStatusLabel.setText("Go: 检查中...");
        swagStatusLabel.setText("Swag: 检查中...");
        vmOptionsStatusLabel.setText("VM Options: 检查中...");
        checkSystemButton.setEnabled(false);
        
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            // 检查Go
            checkGoEnvironment();
            
            // 检查Swag
            checkSwagEnvironment();
            
            // 检查VM选项
            checkVMOptionsEnvironment();
            
            SwingUtilities.invokeLater(() -> checkSystemButton.setEnabled(true));
        });
    }

    // 检查Go环境
    private void checkGoEnvironment() {
        try {
            File goExecutable = PathEnvironmentVariableUtil.findInPath("go");
            if (goExecutable == null) {
                SwingUtilities.invokeLater(() -> {
                    goStatusLabel.setText("Go: ❌ 未安装");
                    goStatusLabel.setForeground(Color.RED);
                });
                return;
            }

            // 获取Go版本
            ProcessBuilder processBuilder = new ProcessBuilder(goExecutable.getAbsolutePath(), "version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                SwingUtilities.invokeLater(() -> {
                    goStatusLabel.setText("Go: ❌ 版本检查失败");
                    goStatusLabel.setForeground(Color.RED);
                });
                return;
            }
            
            String versionOutput = output.toString().trim();
            
            // 解析Go版本号
            Pattern pattern = Pattern.compile("go(\\d+)\\.(\\d+)");
            Matcher matcher = pattern.matcher(versionOutput);
            
            if (matcher.find()) {
                int major = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                
                // 检查是否支持最低版本要求 (Go 1.16+)
                if (major > 1 || (major == 1 && minor >= 16)) {
                    SwingUtilities.invokeLater(() -> {
                        goStatusLabel.setText("Go: ✅ " + versionOutput + " (兼容)");
                        goStatusLabel.setForeground(new Color(0, 128, 0)); // 深绿色
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        goStatusLabel.setText("Go: ⚠️ " + versionOutput + " (版本过低，需要1.16+)");
                        goStatusLabel.setForeground(Color.ORANGE);
                    });
                }
            } else {
                SwingUtilities.invokeLater(() -> {
                    goStatusLabel.setText("Go: ⚠️ " + versionOutput + " (版本解析失败)");
                    goStatusLabel.setForeground(Color.ORANGE);
                });
            }
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                goStatusLabel.setText("Go: ❌ 检查失败: " + e.getMessage());
                goStatusLabel.setForeground(Color.RED);
            });
        }
    }

    // 检查Swag环境
    private void checkSwagEnvironment() {
        try {
            File swagExecutable = PathEnvironmentVariableUtil.findInPath("swag");
            if (swagExecutable == null) {
                SwingUtilities.invokeLater(() -> {
                    swagStatusLabel.setText("Swag: ❌ 未安装");
                    swagStatusLabel.setForeground(Color.RED);
                });
                return;
            }

            // 获取Swag版本
            ProcessBuilder processBuilder = new ProcessBuilder(swagExecutable.getAbsolutePath(), "version");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                SwingUtilities.invokeLater(() -> {
                    swagStatusLabel.setText("Swag: ❌ 版本检查失败");
                    swagStatusLabel.setForeground(Color.RED);
                });
                return;
            }
            
            String versionOutput = output.toString().trim();
            
            SwingUtilities.invokeLater(() -> {
                swagStatusLabel.setText("Swag: ✅ " + versionOutput);
                swagStatusLabel.setForeground(new Color(0, 128, 0)); // 深绿色
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                swagStatusLabel.setText("Swag: ❌ 检查失败: " + e.getMessage());
                swagStatusLabel.setForeground(Color.RED);
            });
        }
    }

    // 检查VM选项环境
    private void checkVMOptionsEnvironment() {
        try {
            // 初始化VM选项设置
            VMOptionsHelper.initializeVMOptions();
            
            // 检查VM选项配置
            boolean isConfigured = VMOptionsHelper.isVMOptionsConfigured();
            String status = VMOptionsHelper.getVMOptionsStatus();
            
            SwingUtilities.invokeLater(() -> {
                if (isConfigured) {
                    vmOptionsStatusLabel.setText("VM Options: ✅ 已配置");
                    vmOptionsStatusLabel.setForeground(new Color(0, 128, 0)); // 深绿色
                } else {
                    vmOptionsStatusLabel.setText("VM Options: ⚠️ " + status);
                    vmOptionsStatusLabel.setForeground(Color.ORANGE);
                }
            });
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> {
                vmOptionsStatusLabel.setText("VM Options: ❌ 检查失败: " + e.getMessage());
                vmOptionsStatusLabel.setForeground(Color.RED);
            });
        }
    }

    // 修改分组容器创建方法
    private JPanel createFullWidthGroup(String title, JComponent... components) {
        JPanel group = new JPanel(new BorderLayout());
        group.setBorder(BorderFactory.createTitledBorder(title));

        // 使用垂直盒子布局作为内容容器
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (JComponent comp : components) {
            // 允许组件保持自然高度
            comp.setMaximumSize(new Dimension(
                    Integer.MAX_VALUE,
                    comp.getPreferredSize().height // 保持组件首选高度
            ));
            content.add(comp);

            // 添加弹性间距（可调整）
            if (comp != components[components.length-1]) {
                content.add(Box.createVerticalStrut(8));
            }
        }

        // 添加底部胶水确保顶部对齐
        content.add(Box.createVerticalGlue());

        // 包裹在滚动面板中（可选）
        JScrollPane scrollWrapper = new JScrollPane(content);
        scrollWrapper.setBorder(BorderFactory.createEmptyBorder());

        group.add(scrollWrapper, BorderLayout.CENTER);
        return group;
    }

    // 修改字段行创建方法（移除高度限制）
    private JPanel createStretchField(String label, JTextField field) {
        JPanel row = new JPanel(new BorderLayout());
        row.add(new JBLabel(label), BorderLayout.WEST);

        // 仅限制最大宽度，允许高度自适应
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        JPanel fieldWrapper = new JPanel(new BorderLayout());
        fieldWrapper.add(field, BorderLayout.CENTER);

        row.add(fieldWrapper, BorderLayout.CENTER);
        return row; // 移除setMaximumSize
    }

    // 修改复选框行创建方法
    private JPanel createCheckboxRow(String label, JCheckBox checkBox) {
        JPanel row = new JPanel(new BorderLayout());
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelPanel.add(new JBLabel(label));

        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkPanel.add(checkBox);

        row.add(labelPanel, BorderLayout.WEST);
        row.add(checkPanel, BorderLayout.CENTER);
        return row; // 移除固定高度限制
    }

    // 创建全宽下拉框
    private JPanel createFullWidthCombo(String label, JComboBox<String> combo) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.add(new JBLabel(label), BorderLayout.WEST);

        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        JPanel comboWrapper = new JPanel(new BorderLayout());
        comboWrapper.add(combo, BorderLayout.CENTER);

        row.add(comboWrapper, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return row;
    }



    public PluginSettingsForm() {
        buildUI();
    }

    // 创建带标题的分组面板
    private JPanel createGroupPanel(String title, JComponent... components) {
        JPanel groupPanel = new JPanel(new BorderLayout());
        groupPanel.setBorder(BorderFactory.createTitledBorder(title));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        for (JComponent comp : components) {
            comp.setMaximumSize(new Dimension(Integer.MAX_VALUE, comp.getPreferredSize().height));
            contentPanel.add(comp);
            contentPanel.add(Box.createVerticalStrut(5));
        }

        groupPanel.add(contentPanel, BorderLayout.CENTER);
        return groupPanel;
    }

    // 创建带标签的输入框面板（带缩进）
    private JPanel createFieldPanel(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 标签设置
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 10);
        panel.add(new JBLabel(labelText), gbc);

        // 输入框设置
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; // 分配额外空间
        textField.setPreferredSize(new Dimension(300, 26)); // 设置首选宽度
        panel.add(textField, gbc);

        // 添加胶水组件保证对齐
        gbc.gridx = 2;
        gbc.weightx = 100.0; // 右侧胶水
        panel.add(Box.createGlue(), gbc);

        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return panel;
    }

    // 创建下拉框面板
    private JPanel createComboPanel(String labelText, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JBLabel(labelText), BorderLayout.WEST);
        panel.add(comboBox, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return panel;
    }

    // 创建复选框面板
    private JPanel createCheckboxPanel(String labelText, JCheckBox checkBox) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JBLabel(labelText), BorderLayout.WEST);
        panel.add(checkBox, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return panel;
    }

    public void setSwagPd(boolean seleted) {
        this.swagPdCheckBox.setSelected(seleted);
    }

    public boolean getSwagPd() {
        return this.swagPdCheckBox.isSelected();
    }

    public String getGoRoot() {
        return goRootField.getText();
    }

    public void setGoRoot(String goRoot) {
        goRootField.setText(goRoot);
    }

    public String getGopath() {
        return gopathField.getText();
    }

    public void setGopath(String gopath) {
        gopathField.setText(gopath);
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