package cn.yapeteam.yolbi;

import cn.yapeteam.loader.logger.Logger;
import cn.yapeteam.yolbi.command.CommandManager;
import cn.yapeteam.yolbi.config.ConfigManager;
import cn.yapeteam.yolbi.event.EventManager;
import cn.yapeteam.yolbi.font.FontManager;
import cn.yapeteam.yolbi.module.ModuleManager;
import cn.yapeteam.yolbi.module.impl.visual.HeadUpDisplay;
import cn.yapeteam.yolbi.notification.NotificationManager;
import cn.yapeteam.yolbi.server.HttpSeverV3;
import cn.yapeteam.yolbi.shader.Shader;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class YolBi {
    public static final YolBi instance = new YolBi();
    public static final String name = "YolBi Lite";
    public static final String version = "0.2.9";
    public static final File YOLBI_DIR = new File(System.getProperty("user.home"), ".yolbi");
    private EventManager eventManager;
    private CommandManager commandManager;
    private ConfigManager configManager;
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

    public static void initialize() {
        boolean ignored = YOLBI_DIR.mkdirs();
        instance.eventManager = new EventManager();
        instance.commandManager = new CommandManager();
        instance.configManager = new ConfigManager();
        instance.moduleManager = new ModuleManager();
        instance.notificationManager = new NotificationManager();
        instance.eventManager.register(instance.commandManager);
        instance.eventManager.register(instance.moduleManager);
        instance.eventManager.register(Shader.class);
        instance.moduleManager.load();
        instance.moduleManager.getModule(HeadUpDisplay.class).setEnabled(true);
        try {
            YolBi.instance.getConfigManager().load();
            instance.httpSeverV3 = new HttpSeverV3(9090);
        } catch (IOException e) {
            Logger.exception(e);
        }
    }

    public void shutdown() {
        try {
            configManager.save();
        } catch (IOException e) {
            Logger.exception(e);
        }
    }
}
