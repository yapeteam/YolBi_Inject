package cn.yapeteam.loader.utils;

import cn.yapeteam.loader.JVMTIWrapper;
import cn.yapeteam.loader.ResourceManager;

public class ClassUtils {
    public static Class<?> getClass(String name) {
        String finalName = name;
        Class<?> clazz = null;
        if (JVMTIWrapper.instance != null) {
            clazz = JVMTIWrapper.instance.getLoadedClasses().stream().filter(c -> finalName.equals(c.getName())).findFirst().orElse(null);
            if (clazz != null) return clazz;
        }
        name = name.replace('/', '.');
        try {
            clazz = Class.forName(name);
        } catch (Throwable ignored) {
        }
        return clazz;
    }

    public static byte[] getClassBytes(String name) {
        return ResourceManager.resources.get(name.replace('.', '/') + ".class");
    }
}
