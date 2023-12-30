package cn.yapeteam.yolbi;

import cn.yapeteam.yolbi.a_pretoload.logger.Logger;
import cn.yapeteam.yolbi.event.EventManager;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventChat;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import lombok.Getter;
import net.minecraft.client.Minecraft;

@Getter
public class YolBi {
    public static final YolBi instance = new YolBi();
    public static final String name = "YolBi Lite";
    public static final String version = "0.0.1";
    private final EventManager eventManager;

    private YolBi() {
        Logger.success("Welcome {} ver {}", name, version);
        eventManager = new EventManager();
        eventManager.register(this);
    }

    @Listener
    private void onRender(EventRender2D e) {
        Minecraft.getMinecraft().fontRendererObj.drawString(name + " " + version, 2, 2, -1, true);
    }

    @Listener
    private void onChat(EventChat e) {
        System.out.println(e.getMessage());
        if (e.getMessage().startsWith(".")) e.setCancelled(true);
    }
}
