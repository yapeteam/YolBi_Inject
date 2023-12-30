package cn.yapeteam.yolbi.a_pretoload;

import cn.yapeteam.yolbi.a_pretoload.logger.Logger;
import cn.yapeteam.yolbi.a_pretoload.utils.ClassUtils;
import org.objectweb.asm.Opcodes;

import java.awt.*;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class Loader {
    public static final int ASM_API = Opcodes.ASM5;
    private static Instrumentation instrumentation;

    public static void preload(Instrumentation instrumentation) {
        Logger.init();
        try {
            Logger.info("Start PreLoading...");
            Loader.instrumentation = instrumentation;
            ClassUtils.instrumentation = instrumentation;
            ClassUtils.instrumentation.addTransformer(new ClassUtils.DumpTransformer(), true);
            Logger.info("Added DumpTransformer");
            Mapper.Mode mode = Mapper.guessMappingMode();
            Logger.info("Reading mappings, mode: {}", mode.name());
            Mapper.setMode(mode);
            Mapper.readMappings();
        } catch (Throwable e) {
            Logger.exception(e);
        }
    }

    public static void start() {
        try {
            Logger.info("Start Loading!");
            Logger.info("Initializing MixinLoader...");
            MixinManager.init();
            Logger.warn("Start transforming!");
            MixinManager.load(instrumentation);
        } catch (Throwable e) {
            Logger.exception(e);
            failed();
        }
    }

    private static void failed() {
        try {
            Logger.writeCache();
            Desktop.getDesktop().open(Logger.getLog());
            System.exit(1);
        } catch (Throwable ignored) {
        }
        try {
            String minecraft = "net.minecraft.client.Minecraft";
            Class<?> Minecraft = ClassUtils.getClass(Mapper.map(null, minecraft, null, Mapper.Type.Class));
            if (Minecraft != null) {
                Method getMinecraft = Minecraft.getMethod(Mapper.map(minecraft, "getMinecraft", null, Mapper.Type.Method));
                Object instance = getMinecraft.invoke(null);
                Method shutdown = Minecraft.getMethod(Mapper.map(minecraft, "shutdown", "()V", Mapper.Type.Method));
                shutdown.invoke(instance);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
