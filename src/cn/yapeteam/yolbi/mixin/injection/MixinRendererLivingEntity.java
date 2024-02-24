package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.mixin.annotations.*;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.render.EventRotationsRender;
import cn.yapeteam.yolbi.utils.misc.ObjectStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;

@Mixin(RendererLivingEntity.class)
@SuppressWarnings({"ParameterCanBeLocal", "UnusedAssignment"})
public class MixinRendererLivingEntity {
    @Shadow
    protected float interpolateRotation(float par1, float par2, float par3) {
        return 0;
    }

    @Inject(
            method = "doRender", desc = "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            target = @Target(value = "INSTANCEOF", shift = Target.Shift.BEFORE)
    )
    public void onRender1(
            @Local(source = "entity", index = 1) EntityLivingBase entity,
            @Local(source = "partialTicks", index = 9) float partialTicks,
            @Local(source = "f", index = 10) float f,
            @Local(source = "f1", index = 11) float f1,
            @Local(source = "f2", index = 12) float f2
    ) {
        EventRotationsRender event = new EventRotationsRender(this.interpolateRotation(entity.prevRotationYawHead, entity.rotationYawHead, partialTicks), this.interpolateRotation(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks), entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, partialTicks);

        if (entity == Minecraft.getMinecraft().thePlayer)
            YolBi.instance.getEventManager().post(event);
        f = event.getBodyYaw();
        f1 = event.getYaw();
        f2 = f1 - f;
        ObjectStore.objects.put("EventRotationsRender", event);
    }

    @Inject(
            method = "doRender", desc = "(Lnet/minecraft/entity/EntityLivingBase;DDDFF)V",
            target = @Target(
                    value = "INVOKEVIRTUAL",
                    target = "net/minecraft/client/renderer/entity/RendererLivingEntity.renderLivingAt(Lnet/minecraft/entity/EntityLivingBase;DDD)V",
                    shift = Target.Shift.BEFORE
            )
    )
    public void onRender2(@Local(source = "f7", index = 13) float f7) {
        f7 = ((EventRotationsRender) ObjectStore.objects.get("EventRotationsRender")).getPitch();
    }
}
