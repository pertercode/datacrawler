import bean.Category;
import org.junit.Test;
import services.HuaMuService;
import services.YuanLinService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WuAgeTester {


    @Test
    public void taaaa() {
        YuanLinService service = new YuanLinService();
        service.requestCompany("http://mxy_yong.yuanlin.com/");

    }

    @Test
    public void runTester() {


        String t = "联系方式第6年萧山天自然园艺场联系人：俞成伟所在地：浙江杭州地址：浙江省萧山区新街镇花木城二期南201号手机：13738015308电话：-认证：";

//        headMatch(t) ;



//
//        int index = t.indexOf(':');
//        if (index < 0) index = t.indexOf('：');
//
//        String head = t.substring(0, index);
//
//
//        int valIndex = t.indexOf('：',index + 1) ;
//
//        String val = t.substring(index + 1,valIndex) ;
//
//        System.out.println(head);
//        System.out.println(val);
//
//
//        int res =  headMatch(head) ;
//
//        if(res > -1){
//
//            if(res == 0){
//                System.out.println("地址");
//                head = head.replaceAll(arr[res],"");
//                System.out.println("转换之前 : " + val);
//
//                val = val.replaceAll(arr[0],"")
//                        .replaceAll(arr[1],"")
//                        .replaceAll(arr[2],"")
//                        .replaceAll(arr[3],"");
//
//                System.out.println("转化之后: " + val.trim());
//            }
//
//        }


    }


    String[] arr = new String[]{
            "地[　| |\\s]*址[　| |\\s]*[：|:]?",
            "联[　| |\\s|  ]*系[　| |\\s|  ]*人[　| |\\s]*[：|:]?",
            "电[　| |\\s]*话[　| |\\s]*[：|:]?",
            "手[　| |\\s]*机[　| |\\s]*[：|:]?"
    };


    public int headMatch(String text) {


        List<Integer> integers = new ArrayList<>();


        for (int i = 0; i < arr.length; i++) {


            Pattern pattern = Pattern.compile(arr[i]);
            Matcher matcher = pattern.matcher(text);

            if (matcher.find()) {
                integers.add(matcher.start());
            }

        }

        System.out.println(integers);

        return -1;
    }


}
