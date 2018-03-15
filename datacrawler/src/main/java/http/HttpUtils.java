package http;

import okhttp3.*;

import java.io.IOException;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

public class HttpUtils {

    // 重试次数
    public static final int retry_count = 16;

    // 重试间隔,单位为秒
    public static final int retry_time = 1;


    // 超时时间，单位为秒
    public static final int connection_time = 8;
    public static final int reader_time = 10;

    private static okhttp3.OkHttpClient mOkHttpClient = null;

    private static okhttp3.OkHttpClient mOkHttpClientNoProxy = null;


    public static synchronized OkHttpClient client() {
        if (mOkHttpClient == null) {
            mOkHttpClient = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(connection_time, TimeUnit.SECONDS)
                    .readTimeout(reader_time, TimeUnit.SECONDS)
                    .proxy(ProxyUtils.getProxy())
                    .proxyAuthenticator(ProxyUtils.getProxyAuthenticator())
                    .build();
        }
        return mOkHttpClient;
    }

    public static synchronized OkHttpClient clientNoProxy() {
        if (mOkHttpClientNoProxy == null) {
            mOkHttpClientNoProxy = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(connection_time, TimeUnit.SECONDS)
                    .readTimeout(reader_time, TimeUnit.SECONDS)
                    .build();
        }
        return mOkHttpClientNoProxy;
    }


//    public static synchronized void setProxy() {
//        Proxy proxy = ProxyUtils.getProxy();
//        if (proxy != null) {
//            mOkHttpClient = mOkHttpClient.newBuilder().proxy(proxy).build();
//        } else {
//            System.err.println("设置代理失败, ProxyUtils.getProxy() 返回 NULL! ");
//        }
//    }


    /**
     * 公共请求头
     *
     * @return
     */
    public static synchronized Headers getCommonHeaders() {
        Headers.Builder builder = new Headers.Builder();
        builder.add("Accept", "*/*");
        builder.add("User-Agent", HttpHeaderUtils.getNextUserAgent());
        builder.add("X-Forwarded-For", IpUtils.getRandomIp());
        return builder.build();
    }


    public static class ResponseWrap {
        public Response response;
        public String body;
        public Exception e;

        public boolean isSuccess() {
            return response != null && response.isSuccessful() && e == null;
        }
    }


    public static synchronized ResponseWrap retryHttp(Request request) {
        return retryHttp(request, "UTF-8");
    }

    public static synchronized ResponseWrap retryHttp(Request request, String charset) {
        ResponseWrap responseWrap = new ResponseWrap();
        for (int i = 0; i < retry_count; i++) {

            Call call = client().newCall(request);
            try {
                responseWrap.response = call.execute();

                byte[] b = responseWrap.response.body().bytes();
                String body = new String(b, charset);
                responseWrap.body = body;
                responseWrap.e = null;
                if (responseWrap.isSuccess())
                    return responseWrap;
            } catch (IOException e) {
                responseWrap.e = e;
            }

            if (retry_time > 0) {
                try {
                    Thread.sleep(retry_time * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseWrap;
    }



    public static synchronized ResponseWrap retryHttpNoProxy(Request request) {
        return retryHttpNoProxy(request, "UTF-8");
    }

    public static synchronized ResponseWrap retryHttpNoProxy(Request request, String charset) {
        ResponseWrap responseWrap = new ResponseWrap();
        for (int i = 0; i < retry_count; i++) {

            Call call = clientNoProxy().newCall(request);
            try {
                responseWrap.response = call.execute();

                byte[] b = responseWrap.response.body().bytes();
                String body = new String(b, charset);
                responseWrap.body = body;
                responseWrap.e = null;
                if (responseWrap.isSuccess())
                    return responseWrap;
            } catch (IOException e) {
                responseWrap.e = e;
            }

            if (retry_time > 0) {
                try {
                    Thread.sleep(retry_time * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return responseWrap;
    }



    public static String errorString(ResponseWrap responseWrap) {
        int code = responseWrap.response.code();
        String url = responseWrap.response.request().url().url().toString();
        String message = responseWrap.response.message();
        String body = responseWrap.body;
        return "[http_error] " + url + "\n" + code + " , " + message + "\n" + body;
    }

    public static String errorStringNoBody(ResponseWrap responseWrap) {
        int code = responseWrap.response.code();
        String url = responseWrap.response.request().url().url().toString();
        String message = responseWrap.response.message();
        return "[http_error] " + url + "\n" + code + " , " + message;
    }


    /**
     * 自动切换代理的请求方法
     *
     * @param request
     * @return
     */
//    public static synchronized ResponseWrap retryHttpAutoProxy(Request request) {
//        ResponseWrap responseWrap = null;
//        for (int i = 0; i < retry_count + 1; i++) {
//            responseWrap = retryHttp(request);
//            if (responseWrap.isSuccess()) return responseWrap;
//
//            if (responseWrap.e instanceof IOException) {
//
//                phpSession = "";
//
//                // 切换IP 然后重试
//                try {
//                    AdslUtils.conn();
//                    AdslUtils.stop();
//                    AdslUtils.conn();
//
////                    System.out.println("切换代理中....");
////
////                    setProxy();
//
//                    if (retry_time > 0) {
//                        try {
//                            Thread.sleep(retry_time * 1000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return responseWrap;
//    }
}
