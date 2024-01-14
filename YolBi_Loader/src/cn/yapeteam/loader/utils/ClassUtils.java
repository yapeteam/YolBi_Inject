package cn.yapeteam.loader.utils;

import cn.yapeteam.loader.NativeWrapper;
import cn.yapeteam.loader.ResourceManager;

public class ClassUtils {
    public static Class<?> getClass(String name) {
        name = name.replace('/', '.');
        if (false) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        String finalName = name;
        return NativeWrapper.getLoadedClasses().stream().filter(c -> finalName.equals(c.getName())).findFirst().orElse(null);
    }

    public static byte[] getClassBytes(String name) {
        name = name.replace('/', '.');
        Class<?> theClass = getClass(name);
        byte[] bytes = NativeWrapper.getClassBytes(theClass);
        if (bytes != null) return bytes;
        else return ResourceManager.resources.get(name);
    }
}
