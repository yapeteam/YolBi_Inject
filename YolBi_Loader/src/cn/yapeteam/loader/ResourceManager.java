package cn.yapeteam.loader;

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
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
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

    /*public static final Map<String, byte[]> resources = new HashMap<>();

    private static byte[] hexStringToBytes(String hexString) {
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            int index = i * 2;
            bytes[i] = (byte) Integer.parseInt(hexString.substring(index, index + 2), 16);
        }
        return bytes;
    }

    public static void add(String name, byte[] bytes) {
        resources.put(name, bytes);
    }

    public static void add(String name, String hex) {
        add(name, hexStringToBytes(hex));
    }*/
}
