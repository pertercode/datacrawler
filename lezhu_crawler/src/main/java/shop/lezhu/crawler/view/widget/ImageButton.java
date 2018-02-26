/*
 * Created by JFormDesigner on Fri Jan 12 16:36:03 CST 2018
 */

package shop.lezhu.crawler.view.widget;

import javax.swing.*;
import java.awt.*;

public class ImageButton extends JButton {
    public ImageButton(ImageIcon icon) {
        setSize(icon.getImage().getWidth(null),
                icon.getImage().getHeight(null));
        setIcon(icon);
        setMargin(new Insets(0, 0, 0, 0));//将边框外的上下左右空间设置为0
        setBorderPainted(false);//不打印边框
//        setBorder(null);//除去边框
        setCursor(new Cursor(Cursor.HAND_CURSOR));
//        setText(null);//除去按钮的默认名称
//        setFocusPainted(false);//除去焦点的框
//        setContentAreaFilled(false);//除去默认的背景填充
    }
}
