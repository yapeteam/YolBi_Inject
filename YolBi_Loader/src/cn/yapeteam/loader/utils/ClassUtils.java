package cn.yapeteam.loader.utils;

import cn.yapeteam.loader.NativeWrapper;
import cn.yapeteam.loader.ResourceManager;

public class ClassUtils {
    public static Class<?> getClass(String name) {
        String finalName = name;
        Class<?> clazz = NativeWrapper.getLoadedClasses().stream().filter(c -> finalName.equals(c.getName())).findFirst().orElse(null);
        if (clazz != null) return clazz;
        name = name.replace('/', '.');
        try {
            clazz = Class.forName(name);
        } catch (Throwable ignored) {
        }
        return clazz;
    }

    public static byte[] getClassBytes(String name) {
        byte[] bytes = NativeWrapper.getClassBytes(getClass(name.replace('/', '.')));
        if (bytes != null) return bytes;
        return ResourceManager.resources.get(name.replace('.', '/') + ".class");
    }
}
