package shop.lezhu.crawler;

import com.yunpian.sdk.model.Result;
import shop.lezhu.crawler.bean.CompanyInfoBean;
import shop.lezhu.crawler.bean.SearchBean;
import shop.lezhu.crawler.services.CompanyInfoService;
import shop.lezhu.crawler.utils.*;
import shop.lezhu.crawler.view.MainForm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Main {

    // 判断是否停止
    public static boolean isStop = true;

    // 判断是否正在搜索
    static boolean searching = false;

    // 队列： 存储待爬虫去爬得关键词队列
    final static BlockingQueue<SearchBean> cacheQueue = new ArrayBlockingQueue<SearchBean>(
            9999);

    /**
     * 线程： 从服务器读取待爬虫爬得关键词，并且存入队列
     */
    public static class ReadKeyRunnable implements Runnable {
        final CompanyInfoService service = new CompanyInfoService();

        @Override
        public void run() {
            mainForm.printLog("开始从 " + ConfigUtils.getApi() + " 轮训 搜索关键词! ");

            while (!isStop) {

                // 如果爬虫没有在搜索,则加入新得关键词
                if (!Main.searching) {

                    List<SearchBean> searchBeans = service.requestSearchBeans();

                    for (int i = 0; i < searchBeans.size(); i++) {
                        SearchBean bean = searchBeans.get(i);
                        if (bean != null) {
                            if (bean.canSearch()) {
                                cacheQueue.add(bean);

                                // 设置关键词抓取完成
                                service.setExecute(bean);
                            }
                        }
                    }

                    if (!isStop) {
                        // 5S 请求一次
                        try {
                            Thread.sleep(5 * 1000);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }

                    if (isStop) break;
                }
            }
            mainForm.printLog("警告： 已停止从 " + ConfigUtils.getApi() + " 轮训 搜索关键词! ");
        }
    }


    // 主窗口程序
    public static MainForm mainForm = null;

    public static String BASE_PATH = null;

    private static Thread readKeysThread = null;


    /**
     * 停止
     */
    private static void stop() {
        mainForm.setTitle(null, null);
        mainForm.setSendingCount(null, null);
        mainForm.setSending(null, null);

        if (!searching && isStop) {
            mainForm.btnStart.setEnabled(true);
        }
    }

    public static void main(String[] args) {

        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        int lastIndex = path.lastIndexOf("/") + 1;
        path = path.substring(0, lastIndex);

        // 初始化基础路径
        BASE_PATH = path;


        mainForm = new MainForm();
        mainForm.onCreate();

        final ReadKeyRunnable readKeyRunnable = new ReadKeyRunnable();
        readKeysThread = new Thread(readKeyRunnable);


        ActionListener actionListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == mainForm.btnStart) {
                    // 开始
                    Main.isStop = false;
                    mainForm.btnStart.setEnabled(false);
                    mainForm.btnStop.setEnabled(true);

                    // 启动线程，获取待搜索得 key
                    if (!readKeysThread.isAlive() || !readKeysThread.isInterrupted()) {
                        readKeysThread = null;
                        readKeysThread = new Thread(readKeyRunnable);
                        readKeysThread.start();
                    }
                } else if (e.getSource() == mainForm.btnStop) {
                    // 停止
                    Main.isStop = true;
                    readKeysThread.interrupt();
                    mainForm.btnStop.setEnabled(false);

                    stop();
                }
            }
        };

        mainForm.btnStart.addActionListener(actionListener);
        mainForm.btnStop.addActionListener(actionListener);


        while (true) {
            if (!isStop) {
                try {
                    SearchBean bean = cacheQueue.take();

                    if (bean != null) {
                        search(bean, bean.getKey(), bean.getLocation());
                        try {
                            Thread.sleep(10 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 根据关键字开始爬虫查询
     *
     * @param key      : 关键字
     * @param location : 位置信息
     */
    public static void search(SearchBean searchBean, String key, String location) {
        searching = true;

        // 发送商家
        int shangjia = 0;

        // 发送总数
        int fasong = 0;

        // 发送成功
        int fasongChengGong = 0;

        // 发送失败
        int fasongShibai = 0;

        // 最大页数
        Integer maxPage = 2;

        // 线程数
        Integer threadSize = 1;

        mainForm.setTitle(key, location);

        mainForm.printLog(">>>>>>>>>>>>>>>爬虫开始>>>>>>>>>>>>>>>>>>>>");
        mainForm.printLog(">>>> key = " + key);
        mainForm.printLog(">>>> location = " + location);
        mainForm.printLog(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        final CompanyInfoService service = new CompanyInfoService();

        // 发送成功得缓存，不会对已发送得手机号进行重复发送
        final List<String> send_phones = new ArrayList<String>();

        // 所有抓取得数据
        List<CompanyInfoBean> companyInfoBeanList = new ArrayList<CompanyInfoBean>();

        for (int page = 1; page <= maxPage && !isStop; page++) {

            // 根据页数查询所有得商家ID
            String[] ids = service.requestComponyIdsWithPage(key, location, page + "");

            if (ids != null && ids.length > 0 && !isStop) {

                for (int i = 1; i <= ids.length && !isStop; i++) {
                    CompanyInfoBean infoBean = null;
                    infoBean = service.requestComponyInfoWithId(ids[i - 1]);
                    if (infoBean != null) {
                        infoBean.setNo(i);
                        infoBean.setKey(key);
                        infoBean.setLocation(location);
                        infoBean.setPage(page);
                        companyInfoBeanList.add(infoBean);
                        shangjia++;


                        // 设置发送信息
                        mainForm.setSending(infoBean.getContactsName(), infoBean.getPhone());
                        mainForm.setSendingCount(i + "", ids.length + "");

                        // 发送短信
                        String msg = ConfigUtils.getMsgTpl();

                        msg = msg.replace("#ContactsName#", infoBean.getContactsName())
                                .replace("#key#", infoBean.getKey())
                                .replace("#goods#", infoBean.getKey())
                                .replace("#duty#", infoBean.getDuty())
                                .replace("#male#", infoBean.getMale())
                                .replace("#areaName#", infoBean.getAreaName());


                        Result result = null;

                        String phone = "";

                        if (ConfigUtils.isDebug()) {

//                            long t = System.currentTimeMillis();
//                            if (t % 2 == 0) {
//                                phone = "15268156868";
//                            } else if (t % 3 == 0) {
//                                phone = "18118552760";
//                            } else if (t % 5 == 0) {
//                                phone = "15062199967";
//                            } else {
//                                phone = "18796213142";
//                            }

                            phone = "18796213142";
                        } else {
                            phone = infoBean.getNumber();
                        }

                        // 未发送得才发送
                        if (!send_phones.contains(phone)) {
                            fasong++;

                            // 设置发送信息
                            mainForm.setSending(infoBean.getContactsName(), phone);
                            mainForm.setSendingCount(i + "'", ids.length + "");

                            result = YunPianSmsUtils.sendSms(phone, msg);

                            if (result != null && result.isSucc()) {
                                send_phones.add(phone);
                                fasongChengGong++;
                                mainForm.printLog(" 发送短信成功, phone : " + phone + " ,  msg = " + msg);
                            } else {
                                fasongShibai++;
                                mainForm.printLog(" 发送短信失败, phone : " + phone + " ,  errorCode =  " + result.getCode() + "  ,  errorMsg =  " + result.getMsg() + " ,  errorDetail =  " + result.getDetail() + "  ,  msg =  " + msg);
                            }
                        }


                    }
                }

                // 每页至少50+数据，如果没到到50+说明没有数据了
                if (ids.length < 50) break;

            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                mainForm.printLog(LogUtils.getStackTraceString(e));
            }
        }

        send_phones.clear();

        if (!isStop) {
            StringBuffer sb = new StringBuffer();

            sb.append("\r\n\r\n");
            sb.append("===========================================");
            sb.append("\r\n");
            sb.append(MainForm.sdf.format(new Date()) + "    ");
            sb.append("\r\n");
            sb.append("===========================================");
            sb.append("\r\n");
            sb.append("发送商家: " + shangjia + "  家");
            sb.append("\r\n");
            sb.append("发送短信: " + fasong + "  条");
            sb.append("\r\n");
            sb.append("成功推送: " + fasongChengGong + "  条");
            sb.append("\r\n");
            sb.append("推送失败: " + fasongShibai + "  条");
            sb.append("\r\n");
            sb.append("===========================================");
            sb.append("\r\n\r\n");

            // 写入报告
            LogUtils.writeSendLog(key, location, sb.toString());

        }


        // 发送商家
        shangjia = 0;
        // 发送总数
        fasong = 0;
        // 发送成功
        fasongChengGong = 0;
        // 发送失败
        fasongShibai = 0;

        if (!isStop) {
            mainForm.printLog(">> 抓取数据总数 ： " + companyInfoBeanList.size());
        } else {
            mainForm.printLog("警告： 由于您点击了停止，已停止抓取数据和发送短信!");
        }


        mainForm.setTitle(null, null);
        mainForm.setSending(null, null);
        mainForm.setSendingCount(null, null);

        searching = false;

        if (isStop)
            stop();
    }
}
