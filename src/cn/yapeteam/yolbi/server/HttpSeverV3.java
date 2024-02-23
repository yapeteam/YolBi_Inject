package cn.yapeteam.yolbi.server;

import cn.yapeteam.loader.ResourceManager;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.notification.Notification;
import cn.yapeteam.yolbi.notification.NotificationType;
import cn.yapeteam.yolbi.utils.animation.Easing;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yuxiangll
 * @since 2024/1/8 07:37
 * IntelliJ IDEA
 */
public class HttpSeverV3 {
    private final ServerSocket serverSocket;

    public HttpSeverV3(final int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        System.out.println("服务器启动");
        ExecutorService executorService = Executors.newCachedThreadPool();
        while (true) {
            Socket clientSocket = serverSocket.accept();
            //System.out.println(clientSocket);
            executorService.execute(() -> process(clientSocket));
        }
    }

    private void process(final Socket clientSocket) {
        try {
            HttpRequest request = HttpRequest.build(clientSocket.getInputStream());
            HttpRespond respond = HttpRespond.build(clientSocket.getOutputStream());
            //System.out.println(request);
            if (request.getMethod() != null) {

                //判断请求是什么方法 不同方法不同处理
                if ("GET".equalsIgnoreCase(request.getMethod())) {
                    doGet(request, respond);
                } else if ("POST".equalsIgnoreCase(request.getMethod())) {
                    doPost(request, respond);
                } else {
                    respond.setHeaders("Content-type", "text/html");
                    respond.setStatue(404);
                    respond.setMessage("No Found");
                    respond.setBody("<h1>No Found</h1>");
                }
                //写入
                respond.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doPost(final HttpRequest request, final HttpRespond respond) throws IOException {
        String userName = request.getPararmeters("username");
        String password = request.getPararmeters("password");
        if (userName == null || password == null) {
            renderHTML(respond, "login.html");
            return;
        }
        if (checkUser(new User(userName, password))) {
            logined();
            renderHTML(respond, "Test.html");
        } else {
            //登录失败
            renderHTML(respond, "login.html");
        }
    }

    private void doGet(final HttpRequest request, final HttpRespond respond) throws IOException {
        String userName = request.getPararmeters("username");
        String password = request.getPararmeters("password");
        User user = new User(userName, password);
        if (userName == null || password == null) {
            renderHTML(respond, "login.html");
            return;
        }
        if (checkUser(user)) {
            logined();
            renderHTML(respond, "Test.html");
        } else {
            renderHTML(respond, "login.html");
        }

    }


    private void renderHTML(final HttpRespond respond, final String name) throws IOException {
        respond.setHeaders("Content-type", "text/html");
        respond.setStatue(200);
        respond.setMessage("Ok");

        InputStream inputStream = getHTML(name);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        //按行读取写入到body中
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            respond.setBody(line);
        }
        bufferedReader.close();
    }

    private void logined() {
        //System.out.println("Logined");
        if (YolBi.instance.getFontManager() == null) return;
        YolBi.instance.getNotificationManager().post(
                new Notification(
                        "Login successfully",
                        Easing.EASE_IN_OUT_QUAD,
                        Easing.EASE_IN_OUT_QUAD,
                        15000L, NotificationType.INIT
                )
        );
    }

    private String getPassword() {
        //todo getPassword
        return "1";
    }

    private String getUsername() {
        //todo getUsername
        return "YolBi";
    }

    private InputStream getHTML(final String name) {
        return new ByteArrayInputStream(ResourceManager.resources.get("web/" + name));
    }

    private boolean checkUser(final User user) {
        return user.getUsername().equals(getUsername()) && user.getPassword().equals(getPassword());
    }


    public static void main(final String[] args) throws IOException {
        /*ResourceManager.add("login.html", Files.readAllBytes(new File("resources/login.html").toPath()));
        ResourceManager.add("Test.html", Files.readAllBytes(new File("resources/Test.html").toPath()));

        HttpSeverV3 v3 = new HttpSeverV3(9090);
        v3.start();*/
    }
}



