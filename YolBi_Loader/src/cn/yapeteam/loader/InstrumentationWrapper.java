package cn.yapeteam.loader;

import lombok.AllArgsConstructor;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InstrumentationWrapper extends JVMTIWrapper {
    private final Instrumentation instrumentation;

    private final Map<Class<?>, byte[]> classfileMap = new HashMap<>();

    public InstrumentationWrapper(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
        instrumentation.addTransformer(new Dumper(classfileMap::put), true);
        JVMTIWrapper.instance = this;
    }

    @Override
    public int redefineClass(Class<?> clazz, byte[] array) {
        ClassDefinition classDefinition = new ClassDefinition(clazz, array);
        try {
            instrumentation.redefineClasses(classDefinition);
        } catch (ClassNotFoundException | UnmodifiableClassException e) {
            return 1;
        }
        return 0;
    }

    @AllArgsConstructor
    private static class Dumper implements ClassFileTransformer {
        public interface Callback {
            void call(Class<?> clazz, byte[] classfile);
        }

        private final Callback callback;

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            callback.call(classBeingRedefined, classfileBuffer);
            return null;
        }
    }

    @Override
    public byte[] getClassBytes(Class<?> clazz) {
        try {
            instrumentation.retransformClasses(clazz);
            while (true) if (classfileMap.containsKey(clazz)) break;
            return classfileMap.get(clazz);
        } catch (UnmodifiableClassException e) {
            return null;
        }
    }

    @Override
    public Class<?> defineClass(ClassLoader loader, byte[] array) {
        return null;
    }

    @Override
    public ArrayList<Class<?>> getLoadedClasses() {
        ArrayList<Class<?>> list = new ArrayList<>();
        //noinspection ManualArrayToCollectionCopy
        for (Class<?> aClass : instrumentation.getAllLoadedClasses())
            list.add(aClass);
        return list;
    }
}
