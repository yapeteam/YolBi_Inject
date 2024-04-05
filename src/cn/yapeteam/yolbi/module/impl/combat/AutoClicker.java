package cn.yapeteam.yolbi.module.impl.combat;

import cn.yapeteam.loader.api.module.ModuleCategory;
import cn.yapeteam.loader.api.module.ModuleInfo;
import cn.yapeteam.loader.api.module.values.impl.BooleanValue;
import cn.yapeteam.loader.api.module.values.impl.NumberValue;
import cn.yapeteam.yolbi.event.Listener;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;
import cn.yapeteam.yolbi.module.Module;
import cn.yapeteam.yolbi.utils.reflect.ReflectUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.Random;

@ModuleInfo(name = "AutoClicker", category = ModuleCategory.COMBAT, key = Keyboard.KEY_F)
public class AutoClicker extends Module {
    private final NumberValue<Integer> min = new NumberValue<>("min", 8, 0, 100, 1);
    private final NumberValue<Integer> max = new NumberValue<>("max", 15, 0, 100, 1);
    private final BooleanValue leftClick = new BooleanValue("leftClick", true),
            rightClick = new BooleanValue("rightClick", false);

    public AutoClicker() {
        min.setCallback((oldV, newV) -> newV > max.getValue() ? oldV : newV);
        max.setCallback((oldV, newV) -> newV < min.getValue() ? oldV : newV);
        addValues(min, max, leftClick, rightClick);
    }

    private long delay = 0, time = 0;

    @Override
    public void onEnable() {
        delay = random(min.getValue(), max.getValue());
        time = System.currentTimeMillis();
    }

    @Listener
    private void onUpdate(EventUpdate e) {
        delay = random(min.getValue(), max.getValue());
        if (mc.currentScreen != null) return;
        if (System.currentTimeMillis() - time >= (1000 / delay)) {
            if (Mouse.isButtonDown(0) && leftClick.getValue()) {
                time = System.currentTimeMillis();
                mc.thePlayer.swingItem();
                ReflectUtil.Minecraft$clickMouse(mc);
            }
            if (Mouse.isButtonDown(1) && rightClick.getValue()) {
                time = System.currentTimeMillis();
                ReflectUtil.Minecraft$rightClickMouse(mc);
            }
        }
    }

    public static long random(double minCPS, double maxCPS) {
        int min = (int) minCPS, max = (int) maxCPS;
        if (maxCPS - minCPS <= 0) return min;
        return newRandom().nextInt((max - min + 1)) + min;
    }

    private static Random newRandom() {
        return new Random((long) (Math.random() * Math.random() * 114514000L));
    }

    @Override
    public String getSuffix() {
        return min.getValue() + "~" + max.getValue() + ":" + delay;
    }
}
