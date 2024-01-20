package cn.yapeteam.loader;

import cn.yapeteam.loader.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceManager {
    public static byte[] readStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try (InputStream input = inStream;
             ByteArrayOutputStream output = outStream) {
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
            return output.toByteArray();
        }
    }

    public static class resources {
        public static InputStream getStream(String name) {
            try (InputStream stream = ResourceManager.class.getResourceAsStream("/" + name)) {
                return stream;
            } catch (IOException e) {
                Logger.exception(e);
            }
            return null;
        }

        public static byte[] get(String name) {
            InputStream stream = getStream(name);
            if (stream != null) {
                try {
                    return readStream(stream);
                } catch (IOException e) {
                    Logger.exception(e);
                }
            }
            return null;
        }
    }
}
