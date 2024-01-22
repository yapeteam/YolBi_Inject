package cn.yapeteam.yolbi;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.mixin.MixinManager;
import cn.yapeteam.yolbi.notification.Notification;
import cn.yapeteam.yolbi.notification.NotificationType;
import cn.yapeteam.yolbi.utils.animation.Easing;

import java.awt.*;
import java.io.File;

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
                            "Injected successfully", "",
                            Easing.EASE_IN_OUT_QUAD,
                            Easing.EASE_IN_OUT_QUAD,
                            15000L, NotificationType.INIT
                    )
            );
            YolBi.instance.getHttpSeverV3().start();
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
