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
    private final NumberValue<Integer> cps = new NumberValue<>("cps", 17, 1, 100, 1);
    private final NumberValue<Double> range = new NumberValue<>("cps range", 1.5, 0.1d, 2.5d, 0.1);
    private final BooleanValue leftClick = new BooleanValue("leftClick", true),
            rightClick = new BooleanValue("rightClick", false);

    public AutoClicker() {
        addValues(cps, range, leftClick, rightClick);
    }

    private double delay = 0, time = 0;

    @Override
    public void onEnable() {
        delay = generate(cps.getValue(), range.getValue());
        time = System.currentTimeMillis();
    }

    private final Random random = new Random();

    public double generateNoise(double min, double max) {
        double u1, u2, v1, v2, s;
        do {
            u1 = random.nextDouble() * 2 - 1;
            u2 = random.nextDouble() * 2 - 1;
            s = u1 * u1 + u2 * u2;
        } while (s >= 1 || s == 0);

        double multiplier = Math.sqrt(-2 * Math.log(s) / s);
        v1 = u1 * multiplier;
        v2 = u2 * multiplier;
        // 将生成的噪声值缩放到指定范围内
        return (v1 + v2) / 2 * (max - min) / 4 + (max + min) / 2;
    }

    private double generate(double cps, double range) {
        double noise = cps;
        for (int j = 0; j < 10; j++) {
            double newNoise = generateNoise(0, cps * 2);
            if (Math.abs(noise - newNoise) < range)
                noise = (noise + newNoise) / 2;
            else j--;
        }
        return noise;
    }

    @Listener
    private void onUpdate(EventUpdate e) {
        delay = generate(cps.getValue(), range.getValue());
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

    @Override
    public String getSuffix() {
        return cps.getValue() + ":" + delay;
    }
}
