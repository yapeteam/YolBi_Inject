package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.mixin.annotations.Inject;
import cn.yapeteam.loader.mixin.annotations.Local;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Target;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {
    @Inject(
            method = "renderTooltip",
            desc = "(Lnet/minecraft/client/gui/ScaledResolution;F)V",
            target = @Target(
                    value = "INVOKESTATIC",
                    target = "net/minecraft/client/renderer/GlStateManager.color(FFFF)V",
                    shift = Target.Shift.AFTER
            )
    )
    //After "this.mc.theWorld.getScoreboard();"
    public void render(@Local(source = "sr", index = 1) ScaledResolution sr, @Local(source = "partialTicks", index = 2) float partialTicks) {
        YolBi.instance.getEventManager().post(new EventRender2D(partialTicks, sr));
    }
}
