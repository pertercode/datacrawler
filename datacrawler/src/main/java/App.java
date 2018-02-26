import crawler.ZhuNiuThread;

public class App {
    public static void main(String[] args) {

        // 从控制台传入参数，根据不同的平台启动不同的抓取程序
        if(args.length > 0){
            String platform = args[0] ;

            if("zhuniu".equals(platform.trim())){
                zhuNiu();
            }

        }else{
            System.err.println("请加入运行参数，以告诉程序需要抓取的平台；");
        }



    }

    public static void zhuNiu (){
        ZhuNiuThread thread = new ZhuNiuThread() ;
        thread.run();
    }
}
