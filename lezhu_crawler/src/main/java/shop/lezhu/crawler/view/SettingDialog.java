/*
 * Created by JFormDesigner on Fri Jan 12 12:28:20 CST 2018
 */

package shop.lezhu.crawler.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author asd
 */
public class SettingDialog extends JDialog {
    public SettingDialog(Frame owner) {
        super(owner);
        initComponents();
    }

    public SettingDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        btnOk = new JButton();
        btnCancle = new JButton();
        pelSetting = new JPanel();
        label1 = new JLabel();
        label2 = new JLabel();
        label3 = new JLabel();
        txtApiKey = new JTextField();
        cbMode = new JComboBox<>();
        scrollPane1 = new JScrollPane();
        txtTpl = new JTextArea();
        label4 = new JLabel();
        label5 = new JLabel();
        label6 = new JLabel();
        label7 = new JLabel();
        label8 = new JLabel();
        label9 = new JLabel();
        lbUrl = new JLabel();
        txtApiUrl = new JTextField();

        //======== this ========
        setFont(this.getFont().deriveFont(this.getFont().getStyle() & ~Font.BOLD));
        setTitle("\u8bbe\u7f6e");
        setName("dialog");
        setResizable(false);
        setAlwaysOnTop(true);
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //---- btnOk ----
        btnOk.setText("\u786e\u5b9a");
        btnOk.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
        contentPane.add(btnOk);
        btnOk.setBounds(345, 420, 95, 45);

        //---- btnCancle ----
        btnCancle.setText("\u53d6\u6d88");
        btnCancle.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
        contentPane.add(btnCancle);
        btnCancle.setBounds(470, 420, 90, 45);

        //======== pelSetting ========
        {
            pelSetting.setLayout(null);

            //---- label1 ----
            label1.setText("\u6a21\u5f0f");
            label1.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
            pelSetting.add(label1);
            label1.setBounds(new Rectangle(new Point(24, 10), label1.getPreferredSize()));

            //---- label2 ----
            label2.setText("\u77ed\u4fe1API_KEY");
            label2.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
            pelSetting.add(label2);
            label2.setBounds(24, 94, 111, 24);

            //---- label3 ----
            label3.setText("\u77ed\u4fe1\u6a21\u677f");
            label3.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
            pelSetting.add(label3);
            label3.setBounds(24, 146, 101, 24);

            //---- txtApiKey ----
            txtApiKey.setText("\u963f\u65af\u987f\u6492");
            txtApiKey.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
            pelSetting.add(txtApiKey);
            txtApiKey.setBounds(150, 92, 410, txtApiKey.getPreferredSize().height);

            //---- cbMode ----
            cbMode.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.BOLD, 18));
            cbMode.setForeground(Color.darkGray);
            cbMode.setModel(new DefaultComboBoxModel<>(new String[] {
                "debug",
                "release"
            }));
            cbMode.setMaximumRowCount(2);
            pelSetting.add(cbMode);
            cbMode.setBounds(150, 6, 160, cbMode.getPreferredSize().height);

            //======== scrollPane1 ========
            {

                //---- txtTpl ----
                txtTpl.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
                scrollPane1.setViewportView(txtTpl);
            }
            pelSetting.add(scrollPane1);
            scrollPane1.setBounds(150, 152, 410, 155);

            //---- label4 ----
            label4.setText(" #ContactsName# \uff1a \u8054\u7cfb\u4eba\u540d\u79f0 \uff08\u5982 \u5f20\u4e09\u3001\u674e\u56db\uff09");
            label4.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 12));
            label4.setForeground(Color.blue);
            label4.setHorizontalAlignment(SwingConstants.LEFT);
            pelSetting.add(label4);
            label4.setBounds(115, 316, 445, 18);

            //---- label5 ----
            label5.setText("#key# \u6216 #goods#\uff1a \u5173\u952e\u8bcd \uff08\u5982 \u6c34\u6ce5\u3001\u9ec4\u6c99\uff09");
            label5.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 12));
            label5.setForeground(Color.blue);
            pelSetting.add(label5);
            label5.setBounds(115, 334, 445, 18);

            //---- label6 ----
            label6.setText("#duty#\uff1a \u804c\u4f4d \uff08\u5982\u603b\u7ecf\u7406\u3001\u8463\u4e8b\u957f\uff09");
            label6.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 12));
            label6.setForeground(Color.blue);
            pelSetting.add(label6);
            label6.setBounds(115, 352, 445, 18);

            //---- label7 ----
            label7.setText("#male#\uff1a \u5148\u751fOR\u5973\u58eb(\u6709\u7684\u4eba\u4e0d\u586b\uff09");
            label7.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 12));
            label7.setForeground(Color.blue);
            pelSetting.add(label7);
            label7.setBounds(115, 370, 445, 18);

            //---- label8 ----
            label8.setText("#areaName#\uff1a \u7ecf\u8425\u8303\u56f4\uff08\u94a2\u7b4b\u3001\u6c34\u6ce5\u3001\u6709\u7684\u4eba\u4e0d\u586b\uff09");
            label8.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 12));
            label8.setForeground(Color.blue);
            pelSetting.add(label8);
            label8.setBounds(115, 388, 445, 18);

            //---- label9 ----
            label9.setText("\u77ed\u4fe1API_KEY");
            label9.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
            pelSetting.add(label9);
            label9.setBounds(0, 411, 111, 24);

            //---- lbUrl ----
            lbUrl.setText("API_URL");
            lbUrl.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
            pelSetting.add(lbUrl);
            lbUrl.setBounds(24, 53, 111, 24);

            //---- txtApiUrl ----
            txtApiUrl.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
            pelSetting.add(txtApiUrl);
            txtApiUrl.setBounds(150, 50, 410, 27);

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < pelSetting.getComponentCount(); i++) {
                    Rectangle bounds = pelSetting.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = pelSetting.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                pelSetting.setMinimumSize(preferredSize);
                pelSetting.setPreferredSize(preferredSize);
            }
        }
        contentPane.add(pelSetting);
        pelSetting.setBounds(0, 0, 580, 410);

        contentPane.setPreferredSize(new Dimension(595, 510));
        pack();
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    public JButton btnOk;
    public JButton btnCancle;
    public JPanel pelSetting;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    public JTextField txtApiKey;
    public JComboBox<String> cbMode;
    private JScrollPane scrollPane1;
    public JTextArea txtTpl;
    private JLabel label4;
    private JLabel label5;
    private JLabel label6;
    private JLabel label7;
    private JLabel label8;
    private JLabel label9;
    private JLabel lbUrl;
    public JTextField txtApiUrl;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
