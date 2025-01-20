package github.com.coderyw.golandswagapifoxplugin;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import okhttp3.*;
import org.json.JSONObject;

public class SwagToolWindowPanel extends JPanel {
    private final Project project;
    private final JTextArea logArea;
    private static final OkHttpClient client = new OkHttpClient();
    private JButton runButton;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SwagToolWindowPanel(Project project) {
        this.project = project;
        this.setLayout(new BorderLayout());
        // 创建顶部选项卡
        JTabbedPane tabbedPane = new JBTabbedPane();
//        tabbedPane.setBackground(Color.WHITE);

        // 创建选项卡上的按钮面板
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // 按钮从左向右排列
//        buttonPanel.setBackground(Color.WHITE);


        runButton = new JButton(AllIcons.Actions.Execute);
        runButton.setPreferredSize(new Dimension(30, 30)); // 设置按钮大小
        // 添加按钮点击事件
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取项目根目录
                ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
                VirtualFile projectRoot = fileIndex.getContentRootForFile(fileIndex.getContentRootForFile(project.getBaseDir()));
                runCommand(projectRoot);
            }
        });

        // 创建停止按钮
        JButton stopButton = new JButton(AllIcons.Actions.Suspend);
        stopButton.setPreferredSize(new Dimension(30, 30)); // 设置按钮大小

        // 添加按钮点击事件
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(SwagToolWindowPanel.this, "Stop Button Clicked!");
            }
        });

        // 将按钮添加到按钮面板
        buttonPanel.add(runButton);
        buttonPanel.add(stopButton);

        // 将按钮面板添加到选项卡
        tabbedPane.addTab("Apifox", buttonPanel);


        // 创建日志输出区域
        logArea = new JTextArea();
        logArea.setEditable(false);

        // 创建一个包含内边距的面板
        JPanel logPanel = new JPanel(new BorderLayout());
//        logPanel.setBackground(Color.BLACK);
        logPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // 设置内边距（上、左、下、右）
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        // 将选项卡和日志区添加到主面板
        add(tabbedPane, BorderLayout.NORTH); // 选项卡放在顶部
        add(logPanel, BorderLayout.CENTER); // 日志区放在中间

    }


    public void runCommand(VirtualFile projectRoot) {
        // 清空日志区域
        logArea.setText("");
        PluginSettings settings = project.getService(PluginSettings.class);
        if (settings == null ) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("Setting get error!", "Error");
            });
            return;
        }
        // 启动新线程来执行命令并实时读取输出
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                // 命令校验
                String swagCmd = swagCheck(settings.goPath, settings.goRoot);

                String path = projectRoot.getPath();
                String[] commands;

                commands = new String[]{swagCmd, "init", path}; // Windows

                // 执行命令
                ProcessBuilder processBuilder = new ProcessBuilder(commands);
                processBuilder.redirectErrorStream(true); // 将标准错误合并到标准输出
                // 设置工作目录（可选）
                processBuilder.directory(new File(path));

                // 启动进程
                Process process = processBuilder.start();

                // 读取标准输出
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line; // 用于 lambda 表达式
                        // 在事件调度线程中更新 UI
                        SwingUtilities.invokeLater(() -> logArea.append(finalLine + "\n"));
                    }
                }

                // 等待进程结束
                int exitCode = process.waitFor();

                send2Apifox(projectRoot);

                SwingUtilities.invokeLater(() -> logArea.append("Process exited with code: " + exitCode + "\n"));
            } catch (Exception exception) {
                // 在事件调度线程中显示错误对话框
                SwingUtilities.invokeLater(() -> {
                    Messages.showErrorDialog("Failed to run system application: " + exception.getMessage(), "Error");
                });
            }
        });
    }

    // 检查文件是否存在，不存在则会通过 go install 安装
    public String swagCheck(String goPath, String goRoot) throws IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        Path swagPath = Paths.get(goPath).resolve("bin").resolve("swag");



        File file = new File(swagPath.toString());
        if (file.exists() && file.isFile()) {
            return swagPath.toString();
        }
        logArea.append("No swag file: "+ swagPath + "\n");
        Path goRun = Paths.get(goRoot).resolve("go");
        // 通过执行go install 安装
        SwingUtilities.invokeLater(() -> logArea.append("Cannot find swag command, go install swag: \n"));
        String command = goRun+" install github.com/swaggo/swag/cmd/swag@latest";
        SwingUtilities.invokeLater(() -> logArea.append("\t"+command+ "\n"));
        ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            String finalLine = line;
            SwingUtilities.invokeLater(() -> logArea.append("\tinstall resp: " + finalLine + "\n"));
        }
        return swagPath.toString();
    }

    public void send2Apifox(VirtualFile projectRoot) {
        // 刷新文件
        projectRoot.refresh(false, false);


        PluginSettings settings = project.getService(PluginSettings.class);
        if (settings == null ) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("Setting get error!", "Error");
            });
            return;
        }
        // 读取文件内容
        VirtualFile folder = projectRoot.findChild("docs"); // 替换为你的文件名
        if (folder == null) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("Folder not found in project root!", "Error");
            });
            return;
        }
        VirtualFile file = folder.findChild("swagger.yaml");
        if (file == null) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("File not found in project root!", "Error");
            });
            return;
        }


        try {
            String fileContent = readFileContent(file);


            JSONObject jsonObject = new JSONObject();
            jsonObject.put("input", fileContent);
            JSONObject options = new JSONObject();
//            if (settings.apiUrl != null) {
//                SwingUtilities.invokeLater(() -> {
//                    Messages.showInfoMessage("Api Url is null!", "Failed");
//                });
//                return;
//            }
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
            SwingUtilities.invokeLater(() -> logArea.append("Request info:\n" + jsonLog + "\n"));

            // 发送 HTTP POST 请求
            //APS-BGF5ogcMOI39Ab3Xb5Mv0QHt9oyIuXvq
            //5683725
            String url = MessageFormat.format("https://api.apifox.com/v1/projects/{0}/import-openapi?locale=zh-CN", settings.projectId);
            sendPostRequest(url, settings.apiKey, String.valueOf(jsonObject));

            SwingUtilities.invokeLater(() -> {
                Messages.showInfoMessage("Successfully generated and uploaded swagger file!", "Success");
            });
        } catch (IOException ex) {
            SwingUtilities.invokeLater(() -> {
                Messages.showErrorDialog("Failed to send file content: " + ex.getMessage(), "Error");
            });
        }
    }

    public void parseResult(JSONObject jsonObject) {
        Object data = jsonObject.get("data");
        JSONObject dataObject = (JSONObject) data;
        Object counters = dataObject.get("counters");
        JSONObject countersObject = (JSONObject) counters;
        SwingUtilities.invokeLater(() -> logArea.append("Final result:\n"));

        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of newly added interfaces(新增接口数量) : {0}\n", countersObject.get("endpointCreated"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of Modified interfaces(修改接口数量) : {0}\n", countersObject.get("endpointUpdated"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of interfaces with import errors(导入出错接口数量) : {0}\n", countersObject.get("endpointFailed"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of ignored interfaces(忽略接口数量) : {0}\n", countersObject.get("endpointIgnored"))));

        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of newly added schemas(新增数据模型数量) : {0}\n", countersObject.get("schemaCreated"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of Modified schemas(修改数据模型数量) : {0}\n", countersObject.get("schemaUpdated"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of schemas with import errors(导入出错数据模型数量) : {0}\n", countersObject.get("schemaFailed"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of ignored schemas(忽略数据模型数量) : {0}\n", countersObject.get("schemaIgnored"))));

        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of newly added interface folder(新增接口目录数量) : {0}\n", countersObject.get("endpointFolderCreated"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of Modified interface folder(修改接口目录数量) : {0}\n", countersObject.get("endpointFolderUpdated"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of interface folder with import errors(导入出错接口目录数量) : {0}\n", countersObject.get("endpointFolderFailed"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of ignored interface folder(忽略接口目录数量) : {0}\n", countersObject.get("endpointFolderIgnored"))));

        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of newly added schema folder(新增数据模型目录数量) : {0}\n", countersObject.get("schemaFolderCreated"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of Modified schema folder(修改数据模型目录数量) : {0}\n", countersObject.get("schemaFolderUpdated"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of schema folder with import errors(导入出错数据模型目录数量) : {0}\n", countersObject.get("schemaFolderFailed"))));
        SwingUtilities.invokeLater(() -> logArea.append(MessageFormat.format("\tNumber of ignored schema folder(忽略数据模型目录数量) : {0}\n", countersObject.get("schemaFolderIgnored"))));


//        SwingUtilities.invokeLater(() -> logArea.append("Api Upload with status:\n" + counters + "\n"));


    }

    public String readFileContent(@NotNull VirtualFile file) throws IOException {
        byte[] contentBytes = file.contentsToByteArray();
        return new String(contentBytes, StandardCharsets.UTF_8);
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

        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("HTTP POST request failed with response code: " + response.code());
            }
            String bodyStr = response.body().string();
            JSONObject jsonObject = new JSONObject(bodyStr);
//            String jsonLog = gson.toJson(jsonObject);
//            SwingUtilities.invokeLater(() -> logArea.append("Api Upload with status:\n" + jsonLog + "\n"));
            parseResult(jsonObject);

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> logArea.append("Api Upload with status: " + e.getMessage() + "\n"));
        }
    }
}