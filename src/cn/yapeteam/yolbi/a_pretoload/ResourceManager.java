package cn.yapeteam.yolbi.a_pretoload;

import java.util.HashMap;
import java.util.Map;

public class ResourceManager {
    public static final Map<String, byte[]> resources = new HashMap<>();

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
    }
}
