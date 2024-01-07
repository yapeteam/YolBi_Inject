package cn.yapeteam.yolbi.a_pretoload.utils;

import cn.yapeteam.yolbi.a_pretoload.ResourceManager;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClassUtils {
    public static Instrumentation instrumentation = null;
    public static final Map<String, byte[]> classBytes = new HashMap<>();

    public static class DumpTransformer implements ClassFileTransformer {

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            ClassUtils.classBytes.put(className.replace('/', '.'), classfileBuffer);
            return classfileBuffer;
        }
    }

    public static Class<?> getClass(String name) {
        name = name.replace('/', '.');
        if (instrumentation == null) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
        String finalName = name;
        return Arrays.stream(instrumentation.getAllLoadedClasses()).filter(c -> finalName.equals(c.getName())).findFirst().orElse(null);
    }

    public static byte[] getClassBytes(String name) throws Throwable {
        name = name.replace('/', '.');
        byte[] bytes = ResourceManager.resources.get(name);
        if (bytes != null) return bytes;
        if (!classBytes.containsKey(name)) {
            Class<?> theClass = getClass(name);
            if (theClass == null) return null;
            instrumentation.retransformClasses(theClass);
        }
        return classBytes.get(name);
    }
}
