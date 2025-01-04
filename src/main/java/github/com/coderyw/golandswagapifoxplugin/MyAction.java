package github.com.coderyw.golandswagapifoxplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;

import java.io.File;

public class MyAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) { // 获取当前项目
        Project project = e.getProject();
        if (project == null) {
            Messages.showErrorDialog("No project found!", "Error");
            return;
        }
        int result = Messages.showYesNoDialog(
                project,
                "Are you sure you want to generate the swagger.yaml file and upload it to Apifox?",
                "Confirmation",
                Messages.getQuestionIcon()
        );

        if (result == Messages.NO) {
           return;
        }


        // 获取项目根目录
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        VirtualFile projectRoot = fileIndex.getContentRootForFile(fileIndex.getContentRootForFile(project.getBaseDir()));
        if (projectRoot != null) {

            // 获取工具窗口
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
            ToolWindow toolWindow = toolWindowManager.getToolWindow("Swag2Apifox");

            if (toolWindow != null) {
                // 激活工具窗口
                toolWindow.show(() -> {
                    // 获取工具窗口的面板
                    SwagToolWindowPanel panel = (SwagToolWindowPanel) toolWindow.getContentManager().getContent(0).getComponent();
                    // 运行命令并显示日志
                    panel.runCommand(projectRoot);
                });
            }
        } else {
            Messages.showErrorDialog("Failed to get project root directory!", "Error");
        }


    }


}