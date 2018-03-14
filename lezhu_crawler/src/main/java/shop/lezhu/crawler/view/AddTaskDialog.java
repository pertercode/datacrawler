/*
 * Created by JFormDesigner on Thu Mar 08 10:30:31 CST 2018
 */

package shop.lezhu.crawler.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author asd
 */
public class AddTaskDialog extends JDialog {
    public AddTaskDialog(Frame owner) {
        super(owner);
        initComponents();
    }

    public AddTaskDialog(Dialog owner) {
        super(owner);
        initComponents();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        label2 = new JLabel();
        label3 = new JLabel();
        label4 = new JLabel();
        txtCInfo = new JTextField();
        txtCDetail = new JTextField();
        txtCPhone = new JTextField();
        label5 = new JLabel();
        txtKey = new JTextField();
        label6 = new JLabel();
        txtLocation = new JTextField();
        cbInfos = new JComboBox();
        label7 = new JLabel();
        label8 = new JLabel();
        txtId = new JTextField();
        buttonBar = new JPanel();
        btnOk = new JButton();
        btnCancel = new JButton();

        //======== this ========
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(null);

                //---- label2 ----
                label2.setText("\u4f01\u4e1a\u4fe1\u606f\uff1a");
                label2.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
                contentPanel.add(label2);
                label2.setBounds(15, 213, 95, 24);

                //---- label3 ----
                label3.setText("\u91c7\u8d2d\u8be6\u60c5\uff1a");
                label3.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
                contentPanel.add(label3);
                label3.setBounds(15, 262, 95, 24);

                //---- label4 ----
                label4.setText("\u8054\u7cfb\u7535\u8bdd\uff1a");
                label4.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
                contentPanel.add(label4);
                label4.setBounds(15, 310, 95, 24);

                //---- txtCInfo ----
                txtCInfo.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
                contentPanel.add(txtCInfo);
                txtCInfo.setBounds(110, 213, 370, 27);

                //---- txtCDetail ----
                txtCDetail.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
                contentPanel.add(txtCDetail);
                txtCDetail.setBounds(110, 263, 370, 27);

                //---- txtCPhone ----
                txtCPhone.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
                contentPanel.add(txtCPhone);
                txtCPhone.setBounds(110, 310, 370, 27);

                //---- label5 ----
                label5.setText("\u5173\u952e\u8bcd\uff1a");
                label5.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
                contentPanel.add(label5);
                label5.setBounds(15, 10, 95, 24);

                //---- txtKey ----
                txtKey.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
                contentPanel.add(txtKey);
                txtKey.setBounds(110, 10, 370, 27);

                //---- label6 ----
                label6.setText("\u5730\u533a\uff1a");
                label6.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
                contentPanel.add(label6);
                label6.setBounds(15, 58, 95, 24);

                //---- txtLocation ----
                txtLocation.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
                txtLocation.setText("\u4e2d\u56fd:\u6c5f\u82cf\u7701:\u5f90\u5dde\u5e02");
                contentPanel.add(txtLocation);
                txtLocation.setBounds(110, 58, 370, 27);

                //---- cbInfos ----
                cbInfos.setPrototypeDisplayValue("asdasd");
                contentPanel.add(cbInfos);
                cbInfos.setBounds(110, 128, 370, cbInfos.getPreferredSize().height);

                //---- label7 ----
                label7.setText("\u91c7\u8d2d\u4fe1\u606f\uff1a");
                label7.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
                contentPanel.add(label7);
                label7.setBounds(15, 126, 90, 24);

                //---- label8 ----
                label8.setText("ID\uff1a");
                label8.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 18));
                contentPanel.add(label8);
                label8.setBounds(15, 170, 95, 24);

                //---- txtId ----
                txtId.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 14));
                txtId.setEditable(false);
                contentPanel.add(txtId);
                txtId.setBounds(110, 170, 370, 27);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < contentPanel.getComponentCount(); i++) {
                        Rectangle bounds = contentPanel.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = contentPanel.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    contentPanel.setMinimumSize(preferredSize);
                    contentPanel.setPreferredSize(preferredSize);
                }
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {0, 85, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {1.0, 0.0, 0.0};

                //---- btnOk ----
                btnOk.setText("\u786e\u5b9a");
                btnOk.setFont(btnOk.getFont().deriveFont(16f));
                buttonBar.add(btnOk, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 5), 0, 0));

                //---- btnCancel ----
                btnCancel.setText("\u53d6\u6d88");
                btnCancel.setFont(new Font("\u5fae\u8f6f\u96c5\u9ed1", Font.PLAIN, 16));
                buttonBar.add(btnCancel, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                    new Insets(0, 0, 0, 0), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JLabel label2;
    private JLabel label3;
    private JLabel label4;
    public JTextField txtCInfo;
    public JTextField txtCDetail;
    public JTextField txtCPhone;
    private JLabel label5;
    public JTextField txtKey;
    private JLabel label6;
    public JTextField txtLocation;
    public JComboBox cbInfos;
    private JLabel label7;
    private JLabel label8;
    public JTextField txtId;
    private JPanel buttonBar;
    public JButton btnOk;
    public JButton btnCancel;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
