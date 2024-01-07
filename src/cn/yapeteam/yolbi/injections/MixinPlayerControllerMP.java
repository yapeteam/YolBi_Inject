package cn.yapeteam.yolbi.injections;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Inject;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Local;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Mixin;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Target;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import cn.yapeteam.yolbi.event.type.CancellableEvent;
import net.minecraft.entity.Entity;

/**
 * @author yuxiangll
 * @since 2024/1/7 20:58
 * IntelliJ IDEA
 */
@Mixin("net.minecraft.client.multiplayer.PlayerControllerMP")
public class MixinPlayerControllerMP {

    @Inject(
            method = "attackEntity",
            desc = "(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/Entity;)V",
            hasReturn = false,
            target = @Target("HEAD")
    )
    public void attackEntity(@Local(source = "targetEntity", index = 2) Entity targetEntity) {
        if (targetEntity != null && ((CancellableEvent) YolBi.instance.getEventManager().post(new EventAttack(targetEntity))).isCancelled())
            return;
        System.currentTimeMillis();
    }
}
