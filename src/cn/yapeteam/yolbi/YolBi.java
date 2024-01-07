package cn.yapeteam.yolbi;

import cn.yapeteam.yolbi.command.CommandManager;
import cn.yapeteam.yolbi.event.EventManager;
import cn.yapeteam.yolbi.font.FontManager;
import cn.yapeteam.yolbi.module.ModuleManager;
import cn.yapeteam.yolbi.module.impl.HeadUpDisplay;
import lombok.Getter;

@Getter
public class YolBi {
    public static YolBi instance = new YolBi();
    public static final String name = "YolBi Lite";
    public static final String version = "0.1.1";
    private final EventManager eventManager;
    private final CommandManager commandManager;
    private final ModuleManager moduleManager;
    private final FontManager fontManager;

    private YolBi() {
        eventManager = new EventManager();
        commandManager = new CommandManager();
        moduleManager = new ModuleManager();
        fontManager = new FontManager();
        eventManager.register(commandManager);
        eventManager.register(moduleManager);
        instance = this;
        moduleManager.getModule(HeadUpDisplay.class).setEnabled(true);
    }
}
