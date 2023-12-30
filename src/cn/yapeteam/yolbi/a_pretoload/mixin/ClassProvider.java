package cn.yapeteam.yolbi.a_pretoload.mixin;

import java.io.IOException;

public interface ClassProvider {
    byte[] getClassBytes(String name) throws Throwable;
}
