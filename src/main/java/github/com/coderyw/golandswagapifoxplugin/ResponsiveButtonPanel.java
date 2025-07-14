package github.com.coderyw.golandswagapifoxplugin;

import com.intellij.icons.AllIcons;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * 响应式按钮面板
 * 根据可用宽度自动调整按钮显示，将隐藏的按钮移到下拉菜单中
 */
public class ResponsiveButtonPanel extends JPanel {
    
    private final List<ButtonInfo> buttons = new ArrayList<>();
    private JButton moreButton;
    private JPopupMenu moreMenu;
    private final FlowLayout flowLayout;
    
    public ResponsiveButtonPanel() {
        flowLayout = new FlowLayout(FlowLayout.RIGHT, 6, 0);
        setLayout(flowLayout);
        moreMenu = new JPopupMenu();
    }
    
    /**
     * 添加按钮到面板
     */
    public void addButton(JButton button, String menuText, Icon menuIcon, ActionListener action) {
        buttons.add(new ButtonInfo(button, menuText, menuIcon, action));
        add(button);
    }
    
    /**
     * 设置更多按钮
     */
    public void setMoreButton(JButton moreButton) {
        this.moreButton = moreButton;
        moreButton.addActionListener(e -> {
            updateMoreMenu();
            moreMenu.show(moreButton, 0, moreButton.getHeight());
        });
        add(moreButton);
    }
    
    /**
     * 更新更多菜单内容
     */
    private void updateMoreMenu() {
        moreMenu.removeAll();
        
        for (ButtonInfo buttonInfo : buttons) {
            if (!buttonInfo.button.isVisible()) {
                JMenuItem menuItem = new JMenuItem(buttonInfo.menuText, buttonInfo.menuIcon);
                menuItem.addActionListener(buttonInfo.action);
                moreMenu.add(menuItem);
            }
        }
    }
    
    /**
     * 根据可用宽度调整按钮显示
     */
    @Override
    public void doLayout() {
        super.doLayout();
        adjustButtonVisibility();
    }
    
    /**
     * 调整按钮可见性
     */
    private void adjustButtonVisibility() {
        if (moreButton == null) return;
        
        int availableWidth = getWidth() - 20; // 留一些边距
        int currentWidth = 0;
        int visibleCount = 0;
        
        // 计算可见按钮的宽度
        for (ButtonInfo buttonInfo : buttons) {
            if (buttonInfo.button.isVisible()) {
                currentWidth += buttonInfo.button.getPreferredSize().width + 6; // 6是按钮间距
                visibleCount++;
            }
        }
        
        // 加上更多按钮的宽度
        currentWidth += moreButton.getPreferredSize().width;
        
        // 如果总宽度超过可用宽度，开始隐藏按钮
        if (currentWidth > availableWidth) {
            // 从最不重要的按钮开始隐藏（从右到左）
            for (int i = buttons.size() - 1; i >= 0; i--) {
                ButtonInfo buttonInfo = buttons.get(i);
                if (buttonInfo.button.isVisible()) {
                    buttonInfo.button.setVisible(false);
                    currentWidth -= buttonInfo.button.getPreferredSize().width + 6;
                    
                    // 重新计算宽度
                    if (currentWidth <= availableWidth) {
                        break;
                    }
                }
            }
        } else {
            // 如果空间足够，尝试显示更多按钮
            for (ButtonInfo buttonInfo : buttons) {
                if (!buttonInfo.button.isVisible()) {
                    int newWidth = currentWidth + buttonInfo.button.getPreferredSize().width + 6;
                    if (newWidth <= availableWidth) {
                        buttonInfo.button.setVisible(true);
                        currentWidth = newWidth;
                    } else {
                        break;
                    }
                }
            }
        }
        
        // 更新更多按钮的可见性
        boolean hasHiddenButtons = false;
        for (ButtonInfo buttonInfo : buttons) {
            if (!buttonInfo.button.isVisible()) {
                hasHiddenButtons = true;
                break;
            }
        }
        moreButton.setVisible(hasHiddenButtons);
    }
    
    /**
     * 按钮信息类
     */
    private static class ButtonInfo {
        final JButton button;
        final String menuText;
        final Icon menuIcon;
        final ActionListener action;
        
        ButtonInfo(JButton button, String menuText, Icon menuIcon, ActionListener action) {
            this.button = button;
            this.menuText = menuText;
            this.menuIcon = menuIcon;
            this.action = action;
        }
    }
} 