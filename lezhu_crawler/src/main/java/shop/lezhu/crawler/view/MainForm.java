package shop.lezhu.crawler.view;

import shop.lezhu.crawler.utils.ConfigUtils;
import shop.lezhu.crawler.utils.LogUtils;
import shop.lezhu.crawler.utils.StringUtils;
import shop.lezhu.crawler.view.widget.ImageButton;
import sun.security.krb5.Config;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainForm extends JFrame implements ActionListener {

    private JPanel jp_content;

    private JPanel jp_head;

    private JPanel jp_bottom;

    private JScrollPane js_scrollpane;

    private JTextArea jta_text;

    private Font font;

    private JLabel jlb_key_title;

    private JLabel jlb_key;

    private JLabel jlb_location_title;

    private JLabel jlb_location;

    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private SettingDialog dialog = null;


    public JButton btnStart = null;

    public JButton btnStop = null;

    ImageButton btnSetting = null;

    ImageButton btnOpenLog = null;

    // 正在发送
    private JLabel lbSending;


    // 数量
    private JLabel lbCount;

    // 窗口初始化
    public void onCreate() {
        int width = 1000;
        int height = 600;

        this.setTitle("乐筑2018");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setSize(width, height);
        // 居中
        center(this);
        this.setMinimumSize(new Dimension(width, height));
        this.setMaximumSize(new Dimension(width, height));
        this.setResizable(false);
        initView();
        this.setVisible(true);

        setTitle(null, null);
        setSending(null, null);
        setSendingCount(null, null);

    }


    /**
     * 初始化视图
     */
    private void initView() {

        font = new Font("Courier New,宋体", 0, 16);

        jp_content = new JPanel();

        // 布局，表格布局
        BorderLayout borderLayout = new BorderLayout();
        jp_content.setLayout(borderLayout);

        this.setContentPane(jp_content);

        // ----------------------

        js_scrollpane = new JScrollPane();
        jp_content.add(js_scrollpane, "Center");


        jta_text = new JTextArea();
        jta_text.setSize(getWidth(), getHeight());
        jta_text.setLineWrap(true);
        jta_text.setWrapStyleWord(true);
        jta_text.setEditable(false);
        jta_text.setFont(font);


        jp_head = new JPanel();
        jp_head.setLayout(null);
        jp_head.setPreferredSize(new Dimension(0, 60));


        int marginLeft = 20;
        int marginTop = 10;

        Font titleFont = new Font("微软雅黑", 0, 16);
        Font titleFontBold = new Font(titleFont.getName(), Font.BOLD, titleFont.getSize());

        jlb_key_title = new JLabel("关键字 : ");
        jlb_key_title.setFont(titleFont);
        jlb_key_title.setBounds(10, 15, 70, 30);
        jp_head.add(jlb_key_title);


        jlb_key = new JLabel("");
        jlb_key.setFont(titleFontBold);
        jlb_key.setForeground(Color.red);
        jlb_key.setHorizontalAlignment(JLabel.LEFT);
        jlb_key.setBounds(jlb_key_title.getX() + jlb_key_title.getWidth(), jlb_key_title.getY(), 200, jlb_key_title.getHeight());
        jp_head.add(jlb_key);


        jlb_location_title = new JLabel("地区 : ");
        jlb_location_title.setFont(titleFont);
        jlb_location_title.setBounds(jlb_key.getX() + jlb_key.getWidth(), 15, jlb_key_title.getWidth(), jlb_key_title.getHeight());
        jp_head.add(jlb_location_title);


        jlb_location = new JLabel("");
        jlb_location.setFont(titleFontBold);
        jlb_location.setForeground(Color.red);
        jlb_location.setHorizontalAlignment(JLabel.LEFT);
        jlb_location.setBounds(jlb_location_title.getX() + jlb_location_title.getWidth(), jlb_location_title.getY(), jlb_key.getWidth(), jlb_location_title.getHeight());
        jp_head.add(jlb_location);


        btnOpenLog = new ImageButton(new ImageIcon(getClass().getClassLoader().getResource("log.png")));
        btnOpenLog.setLocation(this.getWidth() - btnOpenLog.getWidth() - 20, 5);
        btnOpenLog.setToolTipText("打开日志目录");
        btnOpenLog.addActionListener(this);
        jp_head.add(btnOpenLog);


        btnSetting = new ImageButton(new ImageIcon(getClass().getClassLoader().getResource("setting.png")));
        btnSetting.setLocation(btnOpenLog.getLocation().x - btnSetting.getWidth() - 10, 5);
        btnSetting.setToolTipText("打开设置");
        btnSetting.addActionListener(this);
        jp_head.add(btnSetting);


        jp_content.add(jp_head, "North");//将五个普通按钮组件分别按照东、南、西、北、中五个方位添加到中间容器中


        jp_bottom = new JPanel(null);
        jp_bottom.setPreferredSize(new Dimension(this.getWidth(), 60));


        Font btnFont = new Font("微软雅黑", font.getStyle(), 14);

        btnStart = new JButton("开始");
        btnStart.setFont(btnFont);
        btnStart.addActionListener(this);
        btnStart.setBounds(50, 15, 130, 30);
        jp_bottom.add(btnStart);


        btnStop = new JButton("停止");
        btnStop.setFont(btnFont);
        btnStop.setBounds(220, 15, 130, 30);
        btnStop.addActionListener(this);
        jp_bottom.add(btnStop);


        // 正在发送
        lbSending = new JLabel();
        lbSending.setBounds(530, 20, 250, 25);
        lbSending.setFont(new Font(btnFont.getName(), btnFont.getStyle(), 16));
        jp_bottom.add(lbSending);


        // 数量
        lbCount = new JLabel();
        lbCount.setBounds(lbSending.getX() + lbSending.getWidth(), lbSending.getY(), 50, 25);
        jp_bottom.add(lbCount);


        JButton b = new JButton();


        jp_content.add(jp_bottom, "South");
    }


    void initSetting() {
        dialog = new SettingDialog(this);
        dialog.setSize(590, 500);
        dialog.btnOk.addActionListener(this);
        dialog.btnCancle.addActionListener(this);
        dialog.txtApiKey.requestFocus();
        dialog.txtApiKey.setCaretPosition(dialog.txtApiKey.getText().length());

        dialog.txtApiKey.setText(ConfigUtils.getApiKey());
        dialog.cbMode.setSelectedIndex(ConfigUtils.isDebug() ? 0 : 1);

        // 当下拉选择 改变得时间
        dialog.cbMode.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    int index =  dialog.cbMode.getSelectedIndex();
                    if(index == 0){
                        dialog.txtApiUrl.setText(ConfigUtils.DEFAULT_DEBUG_BASEURL);
                    }else if (index == 1){
                        dialog.txtApiUrl.setText(ConfigUtils.DEFAULT_RELEASE_BASEURL);
                    }
                }
            }
        });

        dialog.txtApiUrl.setText(ConfigUtils.getApi());

        dialog.txtTpl.setWrapStyleWord(true);
        dialog.txtTpl.setLineWrap(true);
        dialog.txtTpl.setText(ConfigUtils.getMsgTpl());


        dialog.txtTpl.requestFocus();
        dialog.txtTpl.setCaretPosition(dialog.txtTpl.getText().length());

        dialog.setModal(true);
        dialog.setVisible(true);
    }


    public static void center(Component c) {
        Toolkit kit = Toolkit.getDefaultToolkit();
        int x = (kit.getScreenSize().width - c.getWidth()) / 2;
        int y = (kit.getScreenSize().height - c.getHeight()) / 2;
        c.setLocation(x, y);
    }


    // 按钮点击
    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == btnOpenLog) {
            btnOpenLog.setEnabled(false);
            try {
                Desktop.getDesktop().open(LogUtils.getLogDir());
            } catch (IOException e1) {
                printLog(e1.getMessage(), e1);
            } finally {
                btnOpenLog.setEnabled(true);
            }
        }

        if (e.getSource() == btnSetting) {
            btnSetting.setEnabled(false);

            initSetting();

            btnSetting.setEnabled(true);
        }

        if (dialog != null) {
            if (e.getSource() == dialog.btnOk) {
                Boolean isDebug = dialog.cbMode.getSelectedIndex() < 1 ? true : false;

                String msg_tpl = dialog.txtTpl.getText().trim();

                String apikey = dialog.txtApiKey.getText().trim();

                String api = dialog.txtApiUrl.getText().trim();

                ConfigUtils.setDebug(isDebug);
                ConfigUtils.setApiKey(apikey);
                ConfigUtils.setApi(api);
//                ConfigUtils.setMsgTpl(msg_tpl);

                dialog.dispose();
                dialog = null;

            } else if (e.getSource() == dialog.btnCancle) {
                dialog.dispose();
                dialog = null;
            }
        }

    }

    /**
     * 设置状态
     *
     * @param key
     * @param location
     */
    public void setTitle(String key, String location) {
        if (StringUtils.isEmpty(key)) key = "尚无";
        if (StringUtils.isEmpty(location)) location = "尚无";
        jlb_key.setText(key);
        jlb_location.setText(location);
    }


    /**
     * 设置正在发送得对象
     *
     * @param who   : 人名
     * @param phone ： 手机
     */
    public void setSending(String who, String phone) {
        if (StringUtils.isEmpty(who) || StringUtils.isEmpty(phone)) {
            lbSending.setText("");
        } else {
            lbSending.setText("正在发送: " + who + " | " + phone);

        }
    }

    /**
     * 设置正在发送数量
     *
     * @param current : 当前index
     * @param count   ： 总数
     */
    public void setSendingCount(String current, String count) {
        if (StringUtils.isEmpty(current) || StringUtils.isEmpty(current)) {
            lbCount.setText("");
        } else {
            lbCount.setText("(" + current + "/" + count + ")");

        }
    }


    /**
     * 输出日志
     *
     * @param msg
     */
    public void printLog(String msg) {
        StringBuffer sb = new StringBuffer();

        // 清理文本
        if (jta_text.getLineCount() > 1000) {
            try {
                jta_text.replaceRange("", 0, jta_text.getLineEndOffset(400));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        jta_text.append(sdf.format(new Date()) + "    ");
        jta_text.append(msg);
        jta_text.append("\r\n");


        sb.append(sdf.format(new Date()) + "    ");
        sb.append(msg);
        sb.append("\r\n");

        // 将日志写入文件
        LogUtils.writeToFile(jlb_key.getText().trim(), jlb_location.getText().trim(), sb.toString());


        jta_text.setCaretPosition(jta_text.getText().length());
        js_scrollpane.setViewportView(jta_text);
    }

    public void printLog(String msg, Throwable e) {
        printLog(new String[]{msg}, e);
    }


    /**
     * 输出错误信息
     *
     * @param msg
     * @param e
     */
    public void printLog(String[] msg, Throwable e) {
        StringBuffer sb = new StringBuffer();


        // 清理文本
        if (jta_text.getLineCount() > 1000) {
            try {
                jta_text.replaceRange("", 0, jta_text.getLineEndOffset(400));
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }

        jta_text.append(sdf.format(new Date()) + "    ");
        jta_text.append("=================发现错误==================");
        jta_text.append("\r\n");

        sb.append(sdf.format(new Date()) + "    ");
        sb.append("=================发现错误==================");
        sb.append("\r\n");


        for (String str : msg) {
            jta_text.append(sdf.format(new Date()) + "    ");
            jta_text.append(str);
            jta_text.append("\r\n");

            sb.append(sdf.format(new Date()) + "    ");
            sb.append(str);
            sb.append("\r\n");
        }

        if (e != null) {
            jta_text.append(sdf.format(new Date()) + "    ");
            jta_text.append(LogUtils.getStackTraceString(e));
            jta_text.append("\r\n");

            sb.append(sdf.format(new Date()) + "    ");
            sb.append(LogUtils.getStackTraceString(e));
            sb.append("\r\n");
        }


        jta_text.append(sdf.format(new Date()) + "    ");
        jta_text.append("===========================================");
        jta_text.append("\r\n\r\n");

        sb.append(sdf.format(new Date()) + "    ");
        sb.append("===========================================");
        sb.append("\r\n\r\n");


        // 将日志写入文件
        LogUtils.writeToFile(jlb_key.getText().trim(), jlb_location.getText().trim(), sb.toString());

        jta_text.setCaretPosition(jta_text.getText().length());
        js_scrollpane.setViewportView(jta_text);
    }


}
