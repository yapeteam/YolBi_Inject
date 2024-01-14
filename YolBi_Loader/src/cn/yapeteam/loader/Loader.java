package cn.yapeteam.loader;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.utils.ClassUtils;
import org.objectweb.asm.Opcodes;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class Loader {
    public static final int ASM_API = Opcodes.ASM5;

    public static void preload(String injectionPath) {
        Logger.init();
        try {
            Logger.info("Start PreLoading...");
            Mapper.Mode mode = Mapper.guessMappingMode();
            Logger.info("Reading mappings, mode: {}", mode.name());
            Mapper.setMode(mode);
            Mapper.readMappings();
            Logger.warn("Start Mapping Injection!");
            File injection = new File(injectionPath);
            JarMapper.dispose(injection, new File(injection.getParentFile(), "injection-out.jar"));
            Logger.success("Completed");
        } catch (Throwable e) {
            Logger.exception(e);
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
