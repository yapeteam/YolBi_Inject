package cn.yapeteam.loader;

import cn.yapeteam.loader.logger.Logger;

import java.io.*;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    public static byte[] readStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try (InputStream input = inStream;
             ByteArrayOutputStream output = outStream) {
            while ((len = input.read(buffer)) != -1)
                output.write(buffer, 0, len);
            return output.toByteArray();
        }
    }

    public static class resources {
        public static final Map<String, byte[]> res = new HashMap<>();

        public static InputStream getStream(String name) {
            if (res.containsKey(name))
                return new ByteArrayInputStream(res.get(name));
            File file = new File(Loader.YOLBI_DIR, "resources/" + name);
            try {
                if (file.exists())
                    return Files.newInputStream(file.toPath());
            } catch (IOException e) {
                Logger.exception(e);
            }
            InputStream stream = ResourceManager.class.getResourceAsStream("/" + name);
            if (stream != null)
                return stream;
            Logger.exception(new RuntimeException("Resource not found: " + name));
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
