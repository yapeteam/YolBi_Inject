package cn.yapeteam.loader;

import cn.yapeteam.loader.logger.Logger;
import com.formdev.flatlaf.FlatDarkLaf;
import org.objectweb.asm_9_2.Opcodes;

import javax.swing.*;
import java.awt.*;
import java.io.File;

@SuppressWarnings("unused")
public class Loader {
    public static final int ASM_API = Opcodes.ASM9;
    public static String YOLBI_DIR = null;
    public static Thread client_thread = null;
    public static final int port = 20181;

    public static void preload(String yolbi_dir) {
        Logger.init();
        try {
            if (JVMTIWrapper.instance == null)
                JVMTIWrapper.instance = new NativeWrapper();
            Logger.info("Start PreLoading...");
            YOLBI_DIR = yolbi_dir;
            Mapper.Mode mode = Mapper.guessMappingMode();
            Logger.info("Reading mappings, mode: {}", mode.name());
            Mapper.setMode(mode);
            Mapper.readMappings();
            SocketSender.init();
            try {
                for (Object o : Thread.getAllStackTraces().keySet().toArray()) {
                    Thread thread = (Thread) o;
                    if (thread.getName().equals("Client thread")) {
                        client_thread = thread;
                        UIManager.getDefaults().put("ClassLoader", thread.getContextClassLoader());
                        break;
                    }
                }
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (UnsupportedLookAndFeelException e) {
                Logger.exception(e);
            }
            Logger.warn("Start Mapping Injection!");
            JarMapper.dispose(new File(yolbi_dir, "injection/injection.jar"), new File(yolbi_dir, "injection.jar"));
            Logger.success("Completed");
            Mapper.getCache().clear();
        } catch (Throwable e) {
            Logger.exception(e);
            try {
                Logger.writeCache();
                Desktop.getDesktop().open(Logger.getLog());
            } catch (Throwable ignored) {
            }
        }
    }
}
