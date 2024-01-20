package cn.yapeteam.loader;

import cn.yapeteam.loader.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

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
            File file = new File(Loader.YOLBI_DIR + File.separator + "resources", name);
            if (file.exists())
                try {
                    return Files.newInputStream(file.toPath());
                } catch (IOException e) {
                    Logger.exception(e);
                }
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
