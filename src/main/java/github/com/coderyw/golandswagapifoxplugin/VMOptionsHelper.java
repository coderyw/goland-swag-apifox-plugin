package github.com.coderyw.golandswagapifoxplugin;

import com.intellij.openapi.diagnostic.Logger;

/**
 * VM选项辅助工具类
 * 用于处理IntelliJ平台VM选项相关的警告和错误
 */
public class VMOptionsHelper {
    private static final Logger LOG = Logger.getInstance(VMOptionsHelper.class);
    
    /**
     * 初始化VM选项设置，避免相关警告
     */
    public static void initializeVMOptions() {
        try {
            // 检查并设置VM选项文件属性
            String vmOptionsFile = System.getProperty("jb.vmOptionsFile");
            if (vmOptionsFile == null) {
                // 在开发环境中，这个属性可能为null
                // 设置一个空值来避免警告
                System.setProperty("jb.vmOptionsFile", "");
                LOG.debug("Set jb.vmOptionsFile to empty string to suppress warning");
            }
            
            // 检查其他可能需要的VM选项属性
            String[] vmProperties = {
                "jb.vmOptionsFile",
                "idea.system.path",
                "idea.config.path",
                "idea.log.path"
            };
            
            for (String property : vmProperties) {
                if (System.getProperty(property) == null) {
                    System.setProperty(property, "");
                    LOG.debug("Set " + property + " to empty string");
                }
            }
            
        } catch (SecurityException e) {
            // 如果没有权限设置系统属性，记录警告但不抛出异常
            LOG.warn("Cannot set VM options properties due to security restrictions: " + e.getMessage());
        } catch (Exception e) {
            // 其他异常也记录但不影响插件功能
            LOG.warn("Error initializing VM options: " + e.getMessage());
        }
    }
    
    /**
     * 检查VM选项配置是否正确
     */
    public static boolean isVMOptionsConfigured() {
        try {
            String vmOptionsFile = System.getProperty("jb.vmOptionsFile");
            return vmOptionsFile != null && !vmOptionsFile.isEmpty();
        } catch (Exception e) {
            LOG.warn("Error checking VM options configuration: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 获取VM选项配置状态信息
     */
    public static String getVMOptionsStatus() {
        try {
            String vmOptionsFile = System.getProperty("jb.vmOptionsFile");
            if (vmOptionsFile == null) {
                return "VM options file not configured (jb.vmOptionsFile=null)";
            } else if (vmOptionsFile.isEmpty()) {
                return "VM options file configured but empty";
            } else {
                return "VM options file configured: " + vmOptionsFile;
            }
        } catch (Exception e) {
            return "Error checking VM options: " + e.getMessage();
        }
    }
} 