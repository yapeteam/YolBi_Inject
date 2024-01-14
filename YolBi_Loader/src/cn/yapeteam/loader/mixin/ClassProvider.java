package cn.yapeteam.loader.mixin;

public interface ClassProvider {
    byte[] getClassBytes(String name) throws Throwable;
}
