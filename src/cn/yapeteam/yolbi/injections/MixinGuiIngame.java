package cn.yapeteam.yolbi.injections;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.render.EventRender2D;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Inject;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Local;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Mixin;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Target;
import net.minecraft.client.gui.ScaledResolution;

@Mixin("net.minecraft.client.gui.GuiIngame")
public class MixinGuiIngame {
    @Inject(
            method = "renderGameOverlay",
            desc = "(F)V", hasReturn = false,
            target = @Target(
                    value = "ACONST_NULL",
                    shift = Target.Shift.BEFORE
            )
    )
    //Before "ScoreObjective scoreobjective = null;"
    public void render(@Local(source = "partialTicks", index = 1) float partialTicks, @Local(source = "sr", index = 2) ScaledResolution sr) {
        YolBi.instance.getEventManager().post(new EventRender2D(partialTicks, sr));
    }
}
