package cn.yapeteam.yolbi;

import cn.yapeteam.yolbi.command.CommandManager;
import cn.yapeteam.yolbi.event.EventManager;
import cn.yapeteam.yolbi.font.FontManager;
import cn.yapeteam.yolbi.module.ModuleManager;
import cn.yapeteam.yolbi.module.impl.visual.HeadUpDisplay;
import cn.yapeteam.yolbi.notification.NotificationManager;
import cn.yapeteam.yolbi.server.HttpSeverV3;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class YolBi {
    public static final YolBi instance = new YolBi();
    public static final String name = "YolBi Lite";
    public static final String version = "0.1.1";
    private EventManager eventManager;
    private CommandManager commandManager;
    private ModuleManager moduleManager;
    private FontManager fontManager;
    private NotificationManager notificationManager;
    private HttpSeverV3 httpSeverV3;

    public EventManager getEventManager() {
        if (eventManager == null)
            eventManager = new EventManager();
        return eventManager;
    }

    public FontManager getFontManager() {
        if (fontManager == null)
            fontManager = new FontManager();
        return fontManager;
    }

    public static void initialize(File jar) {
        instance.eventManager = new EventManager();
        instance.commandManager = new CommandManager();
        instance.moduleManager = new ModuleManager(jar);
        instance.notificationManager = new NotificationManager();
        instance.eventManager.register(instance.commandManager);
        instance.eventManager.register(instance.moduleManager);
        instance.moduleManager.getModule(HeadUpDisplay.class).setEnabled(true);
        try {
            instance.httpSeverV3 = new HttpSeverV3(9090);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
