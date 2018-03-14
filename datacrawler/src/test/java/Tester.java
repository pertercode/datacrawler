import com.sun.org.apache.xalan.internal.xsltc.cmdline.Compile;
import http.HttpUtils;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.junit.Test;
import utils.IDUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tester {

    @Test
    public void testYJLProduces() {
//        String url = "http://13328456247.huamu.com/special/contact.html" ;
        String url = "http://hsyyc.huamu.com/special/contact.html";
//        String url = "http://xindaziran.huamu.com/special/contact.html";

        Request request = new Request.Builder().headers(HttpUtils.getCommonHeaders()).url(url).build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            System.out.println("success!");
            Document doc = Jsoup.parse(responseWrap.body);

            findFooter(doc);

            Elements elements = doc.select("ul li label");


            if (elements.size() > 0) {

                Element ul = findParentUl(elements.get(0));
                if (ul != null) {
                    elements = ul.select("li");
                    for (Element e : elements) {

                        String text = e.text().trim();

                        if (text.startsWith("地址")) {
                            int index = text.indexOf('：');
                            String address = text.substring(index + 1);

                            System.out.println(address);
                            break;
                        }
                    }


                } else {
                    System.out.println("not found!");
                }

            }


        } else {
            System.out.println(responseWrap.body + " ," + responseWrap.response.code());
        }

    }

    @Test
    public void testLog() {
        org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Tester.class);
        logger.error("1", new RuntimeException("1"));
    }

    public Element findParentUl(Element element) {
        Element result = null;

        int maxSearchSize = 6;

        for (int i = 0; i < 6; i++) {
            result = element.parent();
            if (result.is("ul"))
                break;
            element = result;
        }

        return result;
    }


    public void findFooter(Element doc) {
        Elements tds = doc.select("div.pp-footer tr td");

        Element td = null;
        if (tds.size() > 0) {
            td = tds.get(0);
        } else {
            Elements elements = doc.select(".footer_cnt p");

            if (elements.size() > 0) {
                td = elements.get(0);
            }
        }
        Element a = td.child(0);

        String cName = a.ownText().trim();

        String href = td.child(1).attr("href").trim();


        String cid = IDUtils.uuid();

        String regex = "store_(\\d+)";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(href);

        if (matcher.find()) {
            cid = matcher.group(1);
        }

        System.out.println("cid : " + cid + " , " + cName);

        String text = td.ownText();

        text = text.replaceAll(Jsoup.parse("&nbsp;").text(), "");

        regex = "所在地区：?(.*)联系人";

        pattern = Pattern.compile(regex);

        matcher = pattern.matcher(text);


        String address = "";

        if (matcher.find()) {
            address = matcher.group(1);
            address = address.trim();
        }

        System.out.println(address);


        regex = "联系人：?(.*)电话";

        pattern = Pattern.compile(regex);

        matcher = pattern.matcher(text);


        String contactName = "";

        String contactPhone = "";

        if (matcher.find()) {
            contactName = matcher.group(1);
            contactName = contactName.trim();
        }

        System.out.println(contactName);


        Elements contactPhones = td.select(".store_mobile");

        if (contactPhones.size() > 0) {
            contactPhone = contactPhones.get(0).text().trim();
            if (contactPhone.contains("*")) {
                contactPhone = getPhone(cid);
            }

        }
        System.out.println(contactPhone);

    }


    public String getPhone(String id) {

        String phone = "";

        String url = "http://www.huamu.com/index.php?app=stats&act=click_log&store_id=" + id + "&callback=receive&sn=&_=" + System.currentTimeMillis();
        Request request = new Request.Builder().headers(HttpUtils.getCommonHeaders())
                .header("Referer", "http://www.huamu.com/")
                .url(url).build();

        HttpUtils.ResponseWrap responseWrap = HttpUtils.retryHttp(request);

        if (responseWrap.isSuccess()) {
            String body = responseWrap.body;

            String regex = "store_mobile\":\"(\\d+)\",";

            Pattern pattern = Pattern.compile(regex);

            Matcher matcher = pattern.matcher(body);


            if (matcher.find()) {
                phone = matcher.group(1);
                phone = phone.trim();
            }

        }

        return phone;
    }

}
