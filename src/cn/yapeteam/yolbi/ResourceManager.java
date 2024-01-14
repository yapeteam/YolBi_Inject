package cn.yapeteam.yolbi;

import cn.yapeteam.loader.logger.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceManager {
    public static class resources {
        private static byte[] readStream(InputStream inStream) throws IOException {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inStream.read(buffer)) != -1)
                outStream.write(buffer, 0, len);
            outStream.close();
            inStream.close();
            return outStream.toByteArray();
        }

        public static byte[] get(String name) {
            try (InputStream stream = ResourceManager.class.getResourceAsStream("/" + name)) {
                if (stream != null)
                    return readStream(stream);
            } catch (IOException e) {
                Logger.exception(e);
            }
            throw new RuntimeException("Resource NOT Found: " + name);
        }
    }
}
