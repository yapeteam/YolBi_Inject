package cn.yapeteam.loader.mixin;

public interface ClassProvider {
    byte[] getClassBytes(Class<?> clazz) throws Throwable;
}
