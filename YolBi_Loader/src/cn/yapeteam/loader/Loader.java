package cn.yapeteam.loader;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.ui.Frame;
import cn.yapeteam.loader.utils.ClassUtils;
import com.formdev.flatlaf.FlatDarkLaf;
import org.objectweb.asm.Opcodes;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class Loader {
    public static final int ASM_API = Opcodes.ASM5;
    public static Frame frame;

    public static void preload(String yolbiDir) {
        Logger.init();
        try {
            Logger.info("Start PreLoading...");
            Mapper.Mode mode = Mapper.guessMappingMode();
            Logger.info("Reading mappings, mode: {}", mode.name());
            Mapper.setMode(mode);
            Mapper.readMappings();
            try {
                for (Object o : Thread.getAllStackTraces().keySet().toArray()) {
                    Thread thread = (Thread) o;
                    if (thread.getName().equals("Client thread"))
                        UIManager.getDefaults().put("ClassLoader", thread.getContextClassLoader());
                }
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (UnsupportedLookAndFeelException e) {
                Logger.exception(e);
            }
            frame = new Frame();
            frame.display();
            Logger.warn("Start Mapping Injection!");
            JarMapper.dispose(new File(yolbiDir, "injection/injection.jar"), new File(yolbiDir, "injection.jar"));
            Logger.success("Completed");
            frame.close();
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
