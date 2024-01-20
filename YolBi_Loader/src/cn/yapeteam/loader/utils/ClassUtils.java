package cn.yapeteam.loader.utils;

import cn.yapeteam.loader.NativeWrapper;
import cn.yapeteam.loader.ResourceManager;

public class ClassUtils {
    public static Class<?> getClass(String name) {
        name = name.replace('/', '.');
        Class<?> clazz = null;
        try {
            clazz = Class.forName(name);
        } catch (Throwable ignored) {
        }
        if (clazz != null) return clazz;
        String finalName = name;
        return NativeWrapper.getLoadedClasses().stream().filter(c -> finalName.equals(c.getName())).findFirst().orElse(null);
    }

    public static byte[] getClassBytes(String name) {
        byte[] bytes = NativeWrapper.getClassBytes(getClass(name.replace('/', '.')));
        if (bytes != null) return bytes;
        return ResourceManager.resources.get(name.replace('.', '/') + ".class");
    }
}
