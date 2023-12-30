package cn.yapeteam.yolbi;

import cn.yapeteam.yolbi.a_pretoload.logger.Logger;
import cn.yapeteam.yolbi.event.EventManager;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.render.EventRender3D;
import lombok.Getter;

@Getter
public class YolBi {
    public static final YolBi instance = new YolBi();
    public static final String name = "YolBi Lite";
    public static final String version = "0.1.1";
    private final EventManager eventManager;

    private YolBi() {
        Logger.success("Welcome {} ver {}", name, version);
        eventManager = new EventManager();
        eventManager.register(this);
    }

    @Listener
    private void onRender(EventRender3D e) {
        System.out.println(e.getPartialTicks());
    }
}
