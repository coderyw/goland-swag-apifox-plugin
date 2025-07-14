package github.com.coderyw.golandswagapifoxplugin;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBTabbedPane;
import com.jgoodies.common.base.Strings;
import github.com.coderyw.model.ApifoxImportSwagger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.*;
import org.json.JSONObject;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import javax.swing.border.Border;
import javax.swing.JTextPane;
import javax.swing.text.*;
import com.intellij.ui.JBColor;

public class SwagToolWindowPanel extends JPanel {
    private final Project project;
    private JTextPane logPane;
    private static final OkHttpClient client = new OkHttpClient();
    private JButton runButton;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SwagToolWindowPanel(Project project) {
        this.project = project;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UIUtil.getPanelBackground());

        // 顶部工具栏（icon按钮，紧密靠左排列）
        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BoxLayout(toolbarPanel, BoxLayout.X_AXIS));
        toolbarPanel.setOpaque(false);
        toolbarPanel.setBorder(JBUI.Borders.empty());

        runButton = new JButton(AllIcons.Actions.Execute);
        runButton.setToolTipText("Generate & Upload");
        styleToolbarButton(runButton);
        runButton.addActionListener(e -> {
            ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
            VirtualFile projectRoot = fileIndex.getContentRootForFile(fileIndex.getContentRootForFile(project.getBaseDir()));
            runCommand(projectRoot);
        });

        JButton stopButton = new JButton(AllIcons.Actions.Suspend);
        stopButton.setToolTipText("Stop");
        styleToolbarButton(stopButton);
        stopButton.addActionListener(e -> JOptionPane.showMessageDialog(SwagToolWindowPanel.this, "Stop operation clicked!"));

        JButton clearButton = new JButton(AllIcons.Actions.GC);
        clearButton.setToolTipText("Clear Log");
        styleToolbarButton(clearButton);
        clearButton.addActionListener(e -> logPane.setText(""));

        toolbarPanel.add(runButton);
        toolbarPanel.add(stopButton);
        toolbarPanel.add(clearButton);

        // 日志区：JTextPane+StyledDocument
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setBackground(getEditorBackground());
        logPane.setForeground(JBColor.foreground());
        logPane.setFont(getConsoleFont());
        logPane.setBorder(JBUI.Borders.empty(4));
        logPane.setMargin(new Insets(0, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(logPane);
        scrollPane.setBorder(JBUI.Borders.empty());
        scrollPane.setBackground(getEditorBackground());
        scrollPane.getViewport().setBackground(getEditorBackground());

        add(toolbarPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建工具栏面板
     */
    private JPanel createToolbarPanel() {
        JPanel toolbarPanel = new JPanel(new BorderLayout());
        toolbarPanel.setBackground(new Color(255, 255, 255));
        toolbarPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            new EmptyBorder(8, 12, 8, 12)
        ));

        // 创建响应式按钮面板
        JPanel buttonPanel = new ResponsiveButtonPanel();
        buttonPanel.setOpaque(false);

        // 创建运行按钮
        runButton = createStyledButton("Generate & Upload", AllIcons.Actions.Execute, new Color(76, 175, 80));
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
                VirtualFile projectRoot = fileIndex.getContentRootForFile(fileIndex.getContentRootForFile(project.getBaseDir()));
                runCommand(projectRoot);
            }
        });

        // 创建停止按钮
        JButton stopButton = createStyledButton("Stop", AllIcons.Actions.Suspend, new Color(244, 67, 54));
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(SwagToolWindowPanel.this, "Stop operation clicked!");
            }
        });

        // 创建清除日志按钮
        JButton clearButton = createStyledButton("Clear Log", AllIcons.Actions.GC, new Color(158, 158, 158));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logPane.setText("");
            }
        });

        // 创建更多操作按钮
        JButton moreButton = createStyledButton("", AllIcons.Actions.More, new Color(158, 158, 158));
        moreButton.setPreferredSize(new Dimension(32, 28));
        moreButton.setToolTipText("More actions");

        // 将按钮添加到响应式面板
        ((ResponsiveButtonPanel) buttonPanel).addButton(clearButton, "Clear Log", AllIcons.Actions.GC, e -> logPane.setText(""));
        ((ResponsiveButtonPanel) buttonPanel).addButton(stopButton, "Stop", AllIcons.Actions.Suspend, e -> JOptionPane.showMessageDialog(SwagToolWindowPanel.this, "Stop operation clicked!"));
        ((ResponsiveButtonPanel) buttonPanel).addButton(runButton, "Generate & Upload", AllIcons.Actions.Execute, e -> {
            ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
            VirtualFile projectRoot = fileIndex.getContentRootForFile(fileIndex.getContentRootForFile(project.getBaseDir()));
            runCommand(projectRoot);
        });
        ((ResponsiveButtonPanel) buttonPanel).setMoreButton(moreButton);

        // 组装工具栏
        toolbarPanel.add(buttonPanel, BorderLayout.EAST);

        return toolbarPanel;
    }
    
    /**
     * 创建日志面板
     */
    private JPanel createLogPanel() {
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBackground(new Color(255, 255, 255));
        logPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
            new EmptyBorder(12, 16, 12, 16)
        ));
        
        // 创建日志标题
        JLabel logTitleLabel = new JLabel("Operation Log");
        logTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logTitleLabel.setForeground(new Color(51, 51, 51));
        logTitleLabel.setIcon(AllIcons.General.ProjectStructure);
        logTitleLabel.setIconTextGap(6);
        
        // 创建日志区域
        logPane = new JTextPane();
        logPane.setEditable(false);
        logPane.setFont(new Font("Consolas", Font.PLAIN, 12));
        logPane.setForeground(new Color(51, 51, 51));
        logPane.setBackground(new Color(248, 248, 248));
        logPane.setCaretColor(new Color(51, 51, 51));
        logPane.setSelectedTextColor(new Color(255, 255, 255));
        logPane.setSelectionColor(new Color(33, 150, 243));
        // JTextPane doesn't support line wrapping directly, use JTextArea methods
        // These lines are removed as they're not applicable to JTextPane
        
        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(logPane);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(200, 200, 200)),
            new EmptyBorder(4, 4, 4, 4)
        ));
        scrollPane.getViewport().setBackground(new Color(248, 248, 248));
        
        // 组装日志面板
        logPanel.add(logTitleLabel, BorderLayout.NORTH);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        return logPanel;
    }
    
    /**
     * 创建样式化按钮
     */
    private JButton createStyledButton(String text, Icon icon, Color backgroundColor) {
        JButton button = new JButton(text, icon);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // 设置按钮最小尺寸
        button.setMinimumSize(new Dimension(80, 28));
        button.setPreferredSize(new Dimension(100, 28));
        
        // 添加鼠标悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.brighter());
            }
            
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }

    /**
     * 设置按钮风格，贴合IDE工具栏
     */
    private void styleToolbarButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setBorder(JBUI.Borders.empty(2));
        button.setFocusable(false);
        button.setOpaque(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setPreferredSize(new Dimension(24, 24));
        button.setMaximumSize(new Dimension(24, 24));
        button.setMinimumSize(new Dimension(24, 24));
    }

    /**
     * 获取IDE当前编辑器背景色
     */
    private Color getEditorBackground() {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        return scheme.getDefaultBackground();
    }

    /**
     * 检查Go版本兼容性
     */
    private boolean checkGoVersion(File goExecutable) {
        try {
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
                appendToLog("Failed to get Go version\n");
                return false;
            }
            
            String versionOutput = output.toString();
            appendToLog("Go version: " + versionOutput);
            
            // 解析Go版本号
            Pattern pattern = Pattern.compile("go(\\d+)\\.(\\d+)");
            Matcher matcher = pattern.matcher(versionOutput);
            
            if (matcher.find()) {
                int major = Integer.parseInt(matcher.group(1));
                int minor = Integer.parseInt(matcher.group(2));
                
                // 检查是否支持最低版本要求 (Go 1.16+)
                if (major > 1 || (major == 1 && minor >= 16)) {
                    appendToLog("Go version is compatible\n");
                    return true;
                } else {
                    appendToLog("Go version too old, requires Go 1.16+\n");
                    return false;
                }
            } else {
                appendToLog("Could not parse Go version\n");
                return false;
            }
        } catch (Exception e) {
            appendToLog("Error checking Go version: " + e.getMessage() + "\n");
            return false;
        }
    }

    /**
     * 设置完整的环境变量
     */
    private void setupEnvironment(ProcessBuilder processBuilder, File goExecutable) {
        Map<String, String> env = processBuilder.environment();
        
        // 获取Go的安装目录
        String goBinPath = goExecutable.getParent();
        String goRoot = new File(goBinPath).getParent();
        
        // 设置GOROOT
        env.put("GOROOT", goRoot);
        
        // 设置GOPATH (如果未设置)
        String currentGopath = env.get("GOPATH");
        if (currentGopath == null || currentGopath.trim().isEmpty()) {
            String userHome = System.getProperty("user.home");
            String defaultGopath = userHome + File.separator + "go";
            env.put("GOPATH", defaultGopath);
            appendToLog("Set GOPATH to: " + defaultGopath + "\n");
        }
        
        // 更新PATH环境变量
        String currentPath = env.get("PATH");
        String newPath = goBinPath + File.pathSeparator + currentPath;
        env.put("PATH", newPath);
        
        appendToLog("Environment setup:\n");
        appendToLog("  GOROOT: " + goRoot + "\n");
        appendToLog("  GOPATH: " + env.get("GOPATH") + "\n");
        appendToLog("  PATH: " + newPath + "\n");
    }

    /**
     * 强制删除并重新生成swagger文件
     */
    private void forceRegenerateSwagger(String projectPath) {
        try {
            appendToLog("Checking for existing swagger files in: " + projectPath + "\n");
            
            File docsDir = new File(projectPath, "docs");
            if (docsDir.exists()) {
                appendToLog("Found existing docs directory\n");
                
                File swaggerYaml = new File(docsDir, "swagger.yaml");
                File swaggerJson = new File(docsDir, "swagger.json");
                File docsGo = new File(docsDir, "docs.go");
                
                // 删除现有的swagger文件
                if (swaggerYaml.exists()) {
                    boolean deleted = swaggerYaml.delete();
                    appendToLog("Deleted existing swagger.yaml: " + deleted + "\n");
                } else {
                    appendToLog("swagger.yaml not found, skipping deletion\n");
                }
                
                if (swaggerJson.exists()) {
                    boolean deleted = swaggerJson.delete();
                    appendToLog("Deleted existing swagger.json: " + deleted + "\n");
                } else {
                    appendToLog("swagger.json not found, skipping deletion\n");
                }
                
                if (docsGo.exists()) {
                    boolean deleted = docsGo.delete();
                    appendToLog("Deleted existing docs.go: " + deleted + "\n");
                } else {
                    appendToLog("docs.go not found, skipping deletion\n");
                }
            } else {
                appendToLog("No existing docs directory found\n");
            }
        } catch (Exception e) {
            appendToLog("Error cleaning up old swagger files: " + e.getMessage() + "\n");
        }
    }

    public void runCommand(VirtualFile projectRoot) {
        // 清空日志区域
        logPane.setText("");
        appendToLog("Starting swagger generation process...\n");
        
        PluginSettings settings = project.getService(PluginSettings.class);
        if (settings == null ) {
            appendToLog("ERROR: Failed to get plugin settings\n");
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("Setting get error!", "Error");
            });
            return;
        }
        
        // 启动新线程来执行命令并实时读取输出
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                appendToLog("Checking system requirements...\n");
                
                // 检查swag命令
                File swagExecutable = PathEnvironmentVariableUtil.findInPath("swag");
                if (swagExecutable == null) {
                    appendToLog("ERROR: swag command not found in PATH\n");
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("The system is missing the swag command, use 'go install github.com/swaggo/swag/cmd/swag@latest' to install\n", "Error");
                    });
                    return;
                }
                appendToLog("Found swag executable: " + swagExecutable.getAbsolutePath() + "\n");

                // 检查Go命令和版本
                File goExecutable = PathEnvironmentVariableUtil.findInPath("go");
                if (goExecutable == null) {
                    appendToLog("ERROR: go command not found in PATH\n");
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("The system is missing the go command, please install golang first\n", "Error");
                    });
                    return;
                }
                appendToLog("Found go executable: " + goExecutable.getAbsolutePath() + "\n");

                // 检查Go版本兼容性
                if (!checkGoVersion(goExecutable)) {
                    appendToLog("ERROR: Go version is not compatible\n");
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("Go version is not compatible. Please upgrade to Go 1.16 or later.\n", "Error");
                    });
                    return;
                }

                String path = projectRoot.getPath();
                appendToLog("Project path: " + path + "\n");
                
                // 强制重新生成swagger文件
                appendToLog("Cleaning up old swagger files...\n");
                forceRegenerateSwagger(path);
                
                // 等待一小段时间确保文件删除完成
                Thread.sleep(500);

                String[] commands;
                if (settings.swagPd) {
                    commands = new String[]{swagExecutable.getAbsolutePath(), "init","--pd", path};
                    appendToLog("Executing: swag init --pd "+path+"\n");
                } else {
                    commands = new String[]{swagExecutable.getAbsolutePath(), "init", path};
                    appendToLog("Executing: swag init "+path+"\n");
                }

                // 执行命令
                ProcessBuilder processBuilder = new ProcessBuilder(commands);
                processBuilder.redirectErrorStream(true); // 将标准错误合并到标准输出

                // 设置完整的环境变量
                setupEnvironment(processBuilder, goExecutable);

                // 设置工作目录
                processBuilder.directory(new File(path));
                appendToLog("Working directory: " + processBuilder.directory().getAbsolutePath() + "\n");

                // 启动进程
                Process process = processBuilder.start();
                appendToLog("Process started, reading output...\n");

                // 读取标准输出
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line; // 用于 lambda 表达式
                        // 在事件调度线程中更新 UI
                        appendToLog(finalLine + "\n");
                        output.append(finalLine).append("\n");
                    }
                }

                // 等待进程结束
                int exitCode = process.waitFor();
                appendToLog("Process completed with exit code: " + exitCode + "\n");

                if (exitCode != 0) {
                    appendToLog("ERROR: Swagger generation failed with exit code: " + exitCode + "\n");
                    appendToLog("Command output:\n" + output.toString() + "\n");
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("Swagger generation failed with exit code: " + exitCode, "Error");
                    });
                    return;
                }

                // 等待文件生成完成
                appendToLog("Waiting for swagger files to be generated...\n");
                Thread.sleep(2000); // 增加等待时间

                // 验证文件是否生成
                File docsDir = new File(path, "docs");
                if (!docsDir.exists()) {
                    appendToLog("ERROR: docs directory was not created\n");
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("docs directory was not created. Please check if swagger generation completed successfully.", "Error");
                    });
                    return;
                }
                
                File swaggerYaml = new File(docsDir, "swagger.yaml");
                File swaggerJson = new File(docsDir, "swagger.json");
                
                appendToLog("Checking generated files:\n");
                appendToLog("  docs directory exists: " + docsDir.exists() + "\n");
                appendToLog("  swagger.yaml exists: " + swaggerYaml.exists() + "\n");
                appendToLog("  swagger.json exists: " + swaggerJson.exists() + "\n");
                
                if (!swaggerYaml.exists() && !swaggerJson.exists()) {
                    appendToLog("ERROR: No swagger files were generated\n");
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("No swagger files were generated. Please check your Go code annotations.", "Error");
                    });
                    return;
                }

                appendToLog("Swagger files generated successfully, proceeding to upload...\n");

                // 在EDT中调用send2Apifox
                ApplicationManager.getApplication().invokeAndWait(() -> {
                    send2Apifox(projectRoot);
                });

            } catch (Exception exception) {
                appendToLog("ERROR: Exception in runCommand: " + exception.getMessage() + "\n");
                exception.printStackTrace();
                // 在事件调度线程中显示错误对话框
                SwingUtilities.invokeLater(() -> {
                    Messages.showErrorDialog("Failed to run system application: " + exception.getMessage(), "Error");
                });
            }
        });
    }

    // 检查文件是否存在，不存在则会通过 go install 安装
    public String swagCheck() throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
//        Path swagPath = Paths.get(goPath).resolve("bin").resolve("swag");
        File pt = PathEnvironmentVariableUtil.findInPath("swag");
        if (pt == null) {
            return "";
        }
        Path swagPath = pt.toPath();


//
//        File file = new File(swagPath.toString());
//        if (file.exists() && file.isFile()) {
//            return swagPath.toString();
//        }


        return swagPath.toString();
//
//        logPane.append("No swag file: "+ swagPath + "\n");
//        Path goRun = Paths.get(goRoot).resolve("go");
//        // 通过执行go install 安装
//        SwingUtilities.invokeLater(() -> logPane.append("Cannot find swag command, go install swag: \n"));
//        String command = goRun+" install github.com/swaggo/swag/cmd/swag@latest";
//        SwingUtilities.invokeLater(() -> logPane.append("\t"+command+ "\n"));
//        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
//        processBuilder.redirectErrorStream(true);
//        Process process = processBuilder.start();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            String finalLine = line;
//            SwingUtilities.invokeLater(() -> logPane.append("\tinstall resp: " + finalLine + "\n"));
//        }
//        return swagPath.toString();
    }

    public void send2Apifox(VirtualFile projectRoot) {
        // 在EDT中执行所有VirtualFile操作
        ApplicationManager.getApplication().invokeAndWait(() -> {
            try {
                appendToLog("Starting file search process...\n");
                
                // 强制刷新文件系统
                projectRoot.refresh(true, true);
                appendToLog("Refreshed project root: " + projectRoot.getPath() + "\n");
                
                // 等待文件系统刷新完成
                try {
                    Thread.sleep(1000); // 增加等待时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                // 查找docs文件夹
                VirtualFile docs = projectRoot.findChild("docs");
                if (docs == null) {
                    appendToLog("ERROR: docs folder not found in project root\n");
                    appendToLog("Available files in project root:\n");
                    VirtualFile[] children = projectRoot.getChildren();
                    for (VirtualFile child : children) {
                        appendToLog("  - " + child.getName() + " (" + (child.isDirectory() ? "dir" : "file") + ")\n");
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("docs folder not found! Please ensure swagger generation completed successfully.", "Error");
                    });
                    return;
                }
                
                appendToLog("Found docs folder: " + docs.getPath() + "\n");
                
                // 强制刷新docs文件夹
                docs.refresh(true, true);
                appendToLog("Refreshed docs folder\n");
                
                // 再次等待
                try {
                    Thread.sleep(1000); // 增加等待时间
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                PluginSettings settings = project.getService(PluginSettings.class);
                if (settings == null ) {
                    appendToLog("ERROR: Failed to get plugin settings\n");
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("Setting get error!", "Error");
                    });
                    return;
                }
                
                // 列出docs文件夹中的所有文件
                appendToLog("Files in docs folder:\n");
                VirtualFile[] docsChildren = docs.getChildren();
                for (VirtualFile child : docsChildren) {
                    appendToLog("  - " + child.getName() + " (" + (child.isDirectory() ? "dir" : "file") + ")\n");
                }
                
                // 检查swagger文件是否存在
                VirtualFile swaggerYaml = docs.findChild("swagger.yaml");
                VirtualFile swaggerJson = docs.findChild("swagger.json");
                
                appendToLog("Checking for swagger files:\n");
                appendToLog("  swagger.yaml exists: " + (swaggerYaml != null && swaggerYaml.exists()) + "\n");
                appendToLog("  swagger.json exists: " + (swaggerJson != null && swaggerJson.exists()) + "\n");
                
                VirtualFile fileToUse = null;
                if (swaggerYaml != null && swaggerYaml.exists()) {
                    fileToUse = swaggerYaml;
                    appendToLog("Using swagger.yaml file\n");
                } else if (swaggerJson != null && swaggerJson.exists()) {
                    fileToUse = swaggerJson;
                    appendToLog("Using swagger.json file\n");
                } else {
                    appendToLog("ERROR: No swagger file found!\n");
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("No swagger file found (swagger.yaml or swagger.json)! Please check if swagger generation completed successfully.", "Error");
                    });
                    return;
                }

                // 读取文件内容
                String fileContent = readFileContent(fileToUse);
                
                // 检查文件内容是否为空
                if (fileContent == null || fileContent.trim().isEmpty()) {
                    appendToLog("ERROR: Swagger file is empty!\n");
                    SwingUtilities.invokeLater(() -> {
                        Messages.showErrorDialog("Swagger file is empty! Please check your Go code annotations.", "Error");
                    });
                    return;
                }

                appendToLog("Swagger file content length: " + fileContent.length() + " characters\n");

                // 在后台线程中发送HTTP请求
                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("input", fileContent);
                        JSONObject options = new JSONObject();
                        if (!Strings.isEmpty(settings.parentFolderId)) {
                            int id = Integer.parseInt(settings.parentFolderId);
                            options.put("targetEndpointFolderId", id);
                        }
                        if (!Strings.isEmpty(settings.mergeSelectedOption)) {
                            options.put("endpointOverwriteBehavior", settings.mergeSelectedOption);
                        }
                        if (settings.isAddBasePath) {
                            options.put("prependBasePath", true);
                        }
                        jsonObject.put("options", options);

                        String jsonLog = gson.toJson(jsonObject);
                        appendToLog("Request info:\n" + jsonLog + "\n");

                        // 发送 HTTP POST 请求
                        String url = MessageFormat.format("https://api.apifox.com/v1/projects/{0}/import-openapi?locale=zh-CN", settings.projectId);
                        sendPostRequest(url, settings.apiKey, String.valueOf(jsonObject));

                        SwingUtilities.invokeLater(() -> {
                            Messages.showInfoMessage("Successfully generated and uploaded swagger file!", "Success");
                        });
                    } catch (IOException ex) {
                        appendToLog("ERROR: Failed to send file content: " + ex.getMessage() + "\n");
                        SwingUtilities.invokeLater(() -> {
                            Messages.showErrorDialog("Failed to send file content: " + ex.getMessage(), "Error");
                        });
                    }
                });

            } catch (Exception e) {
                appendToLog("ERROR: Exception in send2Apifox: " + e.getMessage() + "\n");
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    Messages.showErrorDialog("Error processing swagger files: " + e.getMessage(), "Error");
                });
            }
        });
    }

    public void parseResult(JSONObject jsonObject) {
        Object data = jsonObject.get("data");
        JSONObject dataObject = (JSONObject) data;
        Object counters = dataObject.get("counters");
        JSONObject countersObject = (JSONObject) counters;
        appendToLog("Final result:\n");

        appendToLog(MessageFormat.format("\tNumber of newly added interfaces(新增接口数量) : {0}\n", countersObject.get("endpointCreated")));
        appendToLog(MessageFormat.format("\tNumber of Modified interfaces(修改接口数量) : {0}\n", countersObject.get("endpointUpdated")));
        appendToLog(MessageFormat.format("\tNumber of interfaces with import errors(导入出错接口数量) : {0}\n", countersObject.get("endpointFailed")));
        appendToLog(MessageFormat.format("\tNumber of ignored interfaces(忽略接口数量) : {0}\n", countersObject.get("endpointIgnored")));

        appendToLog(MessageFormat.format("\tNumber of newly added schemas(新增数据模型数量) : {0}\n", countersObject.get("schemaCreated")));
        appendToLog(MessageFormat.format("\tNumber of Modified schemas(修改数据模型数量) : {0}\n", countersObject.get("schemaUpdated")));
        appendToLog(MessageFormat.format("\tNumber of schemas with import errors(导入出错数据模型数量) : {0}\n", countersObject.get("schemaFailed")));
        appendToLog(MessageFormat.format("\tNumber of ignored schemas(忽略数据模型数量) : {0}\n", countersObject.get("schemaIgnored")));

        appendToLog(MessageFormat.format("\tNumber of newly added interface folder(新增接口目录数量) : {0}\n", countersObject.get("endpointFolderCreated")));
        appendToLog(MessageFormat.format("\tNumber of Modified interface folder(修改接口目录数量) : {0}\n", countersObject.get("endpointFolderUpdated")));
        appendToLog(MessageFormat.format("\tNumber of interface folder with import errors(导入出错接口目录数量) : {0}\n", countersObject.get("endpointFolderFailed")));
        appendToLog(MessageFormat.format("\tNumber of ignored interface folder(忽略接口目录数量) : {0}\n", countersObject.get("endpointFolderIgnored")));

        appendToLog(MessageFormat.format("\tNumber of newly added schema folder(新增数据模型目录数量) : {0}\n", countersObject.get("schemaFolderCreated")));
        appendToLog(MessageFormat.format("\tNumber of Modified schema folder(修改数据模型目录数量) : {0}\n", countersObject.get("schemaFolderUpdated")));
        appendToLog(MessageFormat.format("\tNumber of schema folder with import errors(导入出错数据模型目录数量) : {0}\n", countersObject.get("schemaFolderFailed")));
        appendToLog(MessageFormat.format("\tNumber of ignored schema folder(忽略数据模型目录数量) : {0}\n", countersObject.get("schemaFolderIgnored")));


//        SwingUtilities.invokeLater(() -> logPane.append("Api Upload with status:\n" + counters + "\n"));


    }

    public String readFileContent(@NotNull VirtualFile file) throws IOException {
        return com.intellij.openapi.application.ReadAction.compute(() -> {
            try {
                byte[] contentBytes = file.contentsToByteArray();
                return new String(contentBytes, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void sendPostRequest(String url, String auth, String jsonData) throws IOException {
        RequestBody body = RequestBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("X-Apifox-Api-Version", "2024-03-28")
                .addHeader("Authorization", MessageFormat.format("Bearer {0}", auth))
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("HTTP POST request failed with response code: " + response.code());
            }
            
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String bodyStr = responseBody.string();
                JSONObject jsonObject = new JSONObject(bodyStr);
//                String jsonLog = gson.toJson(jsonObject);
//                SwingUtilities.invokeLater(() -> logPane.append("Api Upload with status:\n" + jsonLog + "\n"));
                parseResult(jsonObject);
            } else {
                throw new IOException("Response body is null");
            }
        } catch (IOException e) {
            appendToLog("Api Upload with status: " + e.getMessage() + "\n");
        }
    }

    /**
     * IDEA风格日志输出
     */
    public enum LogType { INFO, SUCCESS, WARN, ERROR }
    public void appendLog(String text, LogType type) {
        StyledDocument doc = logPane.getStyledDocument();
        Style style = logPane.addStyle("log", null);
        switch (type) {
            case INFO:
                StyleConstants.setForeground(style, JBColor.foreground());
                break;
            case SUCCESS:
                StyleConstants.setForeground(style, new JBColor(new Color(0, 153, 0), new Color(0, 200, 0)));
                StyleConstants.setBold(style, true);
                break;
            case WARN:
                StyleConstants.setForeground(style, JBColor.ORANGE);
                StyleConstants.setBold(style, true);
                break;
            case ERROR:
                StyleConstants.setForeground(style, JBColor.RED);
                StyleConstants.setBold(style, true);
                break;
        }
        try {
            doc.insertString(doc.getLength(), text + "\n", style);
        } catch (BadLocationException e) {
            // ignore
        }
        // 自动滚动到底部
        logPane.setCaretPosition(doc.getLength());
    }

    private Font getConsoleFont() {
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        String fontName = scheme.getConsoleFontName();
        int fontSize = scheme.getConsoleFontSize();
        return new Font(fontName, Font.PLAIN, fontSize);
    }
    
    /**
     * Append text to the log pane
     */
    private void appendToLog(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                StyledDocument doc = logPane.getStyledDocument();
                doc.insertString(doc.getLength(), text, null);
                logPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                // ignore
            }
        });
    }
}