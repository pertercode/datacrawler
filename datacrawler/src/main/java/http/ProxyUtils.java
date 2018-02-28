package http;

import okhttp3.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

/**
 * 切换代理工具
 * 阿布云好用 abuyun.com
 */
public class ProxyUtils {

    private static final String order = "860040062614206182";


    // 代理隧道验证信息
    final static String ProxyUser = "H734T8RC07W33G7D";
    final static String ProxyPass = "90D30C8BC19B2430";

    // 代理服务器
    final static String ProxyHost = "http-dyn.abuyun.com";
    final static Integer ProxyPort = 9020;


    public static Proxy getProxy() {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ProxyHost, ProxyPort));
        return proxy;
    }

    public static okhttp3.Authenticator getProxyAuthenticator() {
        return new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                String credential = Credentials.basic(ProxyUser, ProxyPass);
                return response.request().newBuilder()
                        .header("Authorization", credential)
                        .build();
            }
        };
    }

}