package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.module.ModuleCategory;
import cn.yapeteam.yolbi.module.ModuleInfo;
import cn.yapeteam.yolbi.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.utils.reflect.ReflectUtil;
import net.minecraft.network.play.client.C02PacketUseEntity;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Random;

@ModuleInfo(name = "AutoClicker", category = ModuleCategory.COMBAT, key = Keyboard.KEY_F)
public class AutoClicker extends Module {
    private final NumberValue<Integer> min = new NumberValue<>("min", 10, 0, 100, 1);
    private final NumberValue<Integer> max = new NumberValue<>("max", 10, 0, 100, 1);

    public AutoClicker() {
        addValues(min, max);
    }

    private long delay = 0, tim = 0;

    @Override
    public void onEnable() {
        delay = random(min.getValue(), max.getValue());
        tim = System.currentTimeMillis();
    }

    @Listener
    private void onUpdate(EventUpdate e) {
        if (!Mouse.isButtonDown(0) || mc.currentScreen != null) return;
        if (System.currentTimeMillis() - tim >= (1000 / delay)) {
            delay = random(min.getValue(), max.getValue());
            tim = System.currentTimeMillis();
            mc.thePlayer.swingItem();
            if (mc.objectMouseOver.entityHit != null)
                mc.getNetHandler().getNetworkManager().sendPacket(new C02PacketUseEntity(mc.objectMouseOver.entityHit, C02PacketUseEntity.Action.ATTACK));
            else ReflectUtil.Minecraft$clickMouse(mc);
        }
    }

    public static long random(double minCPS, double maxCPS) {
        int mi = Integer.parseInt(String.valueOf(minCPS).replace(".", "/").split("/")[0]), ma = Integer.parseInt(String.valueOf(maxCPS).replace(".", "/").split("/")[0]);
        if (maxCPS - minCPS <= 0) return mi;
        return new Random((long) (Math.random() * 1000)).nextInt((ma - mi)) + mi;
    }

    @Override
    public String getSuffix() {
        return min.getValue() + "~" + max.getValue() + ":" + delay;
    }
}
