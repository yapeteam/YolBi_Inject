package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.mixin.annotations.Inject;
import cn.yapeteam.loader.mixin.annotations.Local;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Target;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.game.EventKey;
import cn.yapeteam.yolbi.event.impl.game.EventTick;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

@Mixin(Minecraft.class)
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
