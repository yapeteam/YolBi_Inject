package cn.yapeteam.yolbi;

import cn.yapeteam.loader.Mapper;
import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.loader.utils.ClassUtils;
import cn.yapeteam.yolbi.mixin.MixinManager;
import cn.yapeteam.yolbi.notification.Notification;
import cn.yapeteam.yolbi.notification.NotificationType;
import cn.yapeteam.yolbi.utils.animation.Animation;
import cn.yapeteam.yolbi.utils.animation.Easing;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class Loader {
    public static void start(String jarPath) {
        try {
            Logger.info("Start Loading!");
            Logger.info("Initializing MixinLoader...");
            MixinManager.init();
            Logger.warn("Start transforming!");
            MixinManager.load();
            Logger.success("Welcome {} ver {}", YolBi.name, YolBi.version);
            YolBi.initialize(new File(jarPath));
            YolBi.instance.getNotificationManager().post(
                    new Notification(
                            "Injected successfully",
                            new Animation(Easing.EASE_IN_OUT_CIRC, 15000L),
                            NotificationType.INIT
                    )
            );
            YolBi.instance.getHttpSeverV3().start();
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
