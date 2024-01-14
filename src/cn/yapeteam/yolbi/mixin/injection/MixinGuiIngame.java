package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.mixin.annotations.Inject;
import cn.yapeteam.loader.mixin.annotations.Local;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Target;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import net.minecraft.client.gui.ScaledResolution;

@Mixin("net.minecraft.client.gui.GuiIngame")
public class MixinGuiIngame {
    @Inject(
            method = "renderGameOverlay",
            desc = "(F)V", hasReturn = false,
            target = @Target(
                    value = "INVOKEVIRTUAL",
                    target = "net/minecraft/client/multiplayer/WorldClient.getScoreboard()Lnet/minecraft/scoreboard/Scoreboard;",
                    shift = Target.Shift.AFTER
            )
    )
    //After "this.mc.theWorld.getScoreboard();"
    public void render(@Local(source = "partialTicks", index = 1) float partialTicks, @Local(source = "sr", index = 2) ScaledResolution sr) {
        YolBi.instance.getEventManager().post(new EventRender2D(partialTicks, sr));
    }
}
