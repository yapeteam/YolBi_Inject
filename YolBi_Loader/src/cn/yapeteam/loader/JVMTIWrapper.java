package cn.yapeteam.loader;

import java.util.ArrayList;

@SuppressWarnings("unused")
public abstract class JVMTIWrapper {
    public static JVMTIWrapper instance = null;

    public abstract int redefineClass(Class<?> clazz, byte[] array);

    public abstract byte[] getClassBytes(Class<?> clazz);

    public abstract Class<?> defineClass(ClassLoader loader, byte[] array);

    public abstract ArrayList<Class<?>> getLoadedClasses();
}
