package cn.yapeteam.yolbi.injections;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Inject;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Local;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Mixin;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Target;
import cn.yapeteam.yolbi.event.impl.game.EventKey;
import cn.yapeteam.yolbi.event.impl.game.EventTick;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

@Mixin("net.minecraft.client.Minecraft")
public class MixinMinecraft {
    @Inject(method = "runTick", desc = "()V", hasReturn = false, target = @Target("HEAD"))
    public void onTick() {
        YolBi.instance.getEventManager().post(new EventTick());
    }

    @Inject(method = "runTick", desc = "()V", hasReturn = false, target = @Target(value = "INVOKESTATIC", target = "org/lwjgl/input/Keyboard.getEventKeyState()Z", shift = Target.Shift.AFTER))
    public void onKey(@Local(source = "key", index = 1) int key) {
        if (Minecraft.getMinecraft().currentScreen == null && Keyboard.getEventKeyState())
            YolBi.instance.getEventManager().post(new EventKey(key));
    }
}
