package cn.yapeteam.yolbi.server;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yuxiangll
 * @since 2024/1/8 07:34
 * IntelliJ IDEA
 */
public class HttpRequest {
    // 给这个类构造一些 getter 方法. (不要搞 setter).
    // 请求对象的内容应该是从网络上解析来的. 用户不应该修改.
    @Getter
    private String method;
    @Getter
    private String url;
    @Getter
    private String version;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> parameters = new HashMap<>();
    private Map<String, String> cookies = new HashMap<>();
    @Getter
    private String body;
    // 请求的构造逻辑, 也使用工厂模式来构造.
    // 此处的参数, 就是从 socket 中获取到的 InputStream 对象
    // 这个过程本质上就是在 "反序列化"
    public static HttpRequest build(InputStream inputStream) throws IOException {
        HttpRequest request = new HttpRequest();
        // 此处的逻辑中, 不能把 bufferedReader 写到 try ( ) 中.
        // 一旦写进去之后意味着 bufferReader 就会被关闭, 会影响到 clientSocket 的状态.
        // 等到最后整个请求处理完了, 再统一关闭
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        String firstLine = bufferedReader.readLine();
        if (firstLine != null) {
            // 此处的 build 的过程就是解析请求的过程.
            // 1. 解析首行
            //解析得方法 url 和 版本
            String[] firstLineTokens = firstLine.split(" ");
            request.method = firstLineTokens[0];
            request.url = firstLineTokens[1];
            request.version = firstLineTokens[2];
            //解析url中的键值对
            int pos = request.url.indexOf("?");
            if (pos != -1) {
                // 看看 url 中是否有 ? . 如果没有, 就说明不带参数, 也就不必解析了
                // 此处的 parameters 是希望包含整个 参数 部分的内容
                // pos 表示 ? 的下标
                // /index.html?a=10&b=20
                // parameters 的结果就相当于是 a=10&b=20
                String parameters = request.url.substring(pos + 1);
                // 切分的最终结果, key a, value 10; key b, value 20;
                parseKV(parameters, request.parameters);
            }
            //解析headers
            String line = "";
            while ((line = bufferedReader.readLine()) != null && line.length() != 0) {
                String[] result = line.split(": ");
                request.headers.put(result[0], result[1]);
            }
            //解析cookie
            String cookie = request.headers.get("Cookie");
            if (cookie != null) {
                parseCookie(cookie, request.cookies);
            }
            //解析body
            if ("POST".equalsIgnoreCase(request.method)
                    || "PUT".equalsIgnoreCase(request.method)) {
                //暂时只考虑这俩个方法的body
                int length = Integer.parseInt(request.headers.get("Content-Length"));
                char[] buffer = new char[length];
                int len = bufferedReader.read(buffer);
                request.body = new String(buffer, 0, len);
                parseKV(request.body, request.parameters);
            }
        }
        return request;
    }

    private static void parseCookie(String cookie, Map<String, String> cookies) {
        //在这里解析键值对
        //先按; 分割
        //再按=分割
        String[] kv = cookie.split("; ");
        for (String s : kv
        ) {
            String[] result = s.split("=");
            cookies.put(result[0], result[1]);
        }
    }

    private static void parseKV(String parameters, Map<String, String> parameters1) {
        //在这里解析键值对
        //先按&分割
        //再按=分割
        String[] kv = parameters.split("&");
        for (String s : kv
        ) {
            String[] result = s.split("=");
            parameters1.put(result[0], result[1]);
        }
    }

    public String getHeaders(String key) {
        return headers.get(key);
    }

    // 此处的 getter 手动写, 自动生成的版本是直接得到整个 hash 表.
    // 而我们需要的是根据 key 来获取值.
    public String getCookie(String key) {
        return cookies.get(key);
    }

    public String getPararmeters(String key) {
        return parameters.get(key);
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
                "method '" + method + '\'' +
                ", url '" + url + '\'' +
                ", version '" + version + '\'' +
                ", headers " + headers +
                ", parameters " + parameters +
                ", cookies " + cookies +
                ", body '" + body + '\'' +
                '}';
    }
}

