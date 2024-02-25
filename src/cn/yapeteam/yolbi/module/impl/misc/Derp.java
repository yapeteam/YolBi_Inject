package cn.yapeteam.yolbi.module.impl.misc;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventMotion;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.ModuleInfo;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;

@ModuleInfo(name = "Derp", category = ModuleCategory.MISC)
public class Derp extends Module {
    private float yaw;
    private long time;
    private final NumberValue<Float> speed = new NumberValue<>("speed", 1f, 0f, 10f, 1f);
    private final NumberValue<Float> pitch = new NumberValue<>("pitch", 90f, 0f, 360f, 1f);

    {
        addValues(speed, pitch);
    }

    @Override
    protected void onEnable() {
        if (mc.thePlayer != null)
            yaw = mc.thePlayer.rotationYaw;
        time = System.currentTimeMillis();
    }

    @Listener
    private void onMotion(EventMotion e) {
        if ((System.currentTimeMillis() - time) / 1000f >= 1 / speed.getValue())
            time = System.currentTimeMillis();
        yaw = 360f * ((System.currentTimeMillis() - time) / (1000f / speed.getValue()));
        e.setPitch(pitch.getValue());
        e.setYaw(yaw);
    }
}
