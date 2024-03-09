package cn.yapeteam.loader;

import cn.yapeteam.loader.logger.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketSender {
    private static Socket socket = null;
    private static PrintWriter writer = null;

    public static void init() {
        try {
            socket = new Socket("127.0.0.1", Loader.port);
            OutputStream stream = socket.getOutputStream();
            writer = new PrintWriter(stream);
        } catch (Throwable e) {
            Logger.exception(e);
        }
    }

    public static void send(String message) {
        try {
            if (writer != null) {
                writer.println(message);
                writer.flush();
            }
        } catch (Throwable e) {
            Logger.exception(e);
        }
    }

    public static void close() {
        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                Logger.exception(e);
            }
        }
    }
}
