package cn.yapeteam.yolbi.injections;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Inject;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Local;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Mixin;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Target;
import cn.yapeteam.yolbi.event.impl.player.EventAttack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.world.WorldSettings;

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
    public void attackEntity(EntityPlayer playerIn, Entity targetEntity) {
        if (targetEntity != null) {
            EventAttack attackEvent = new EventAttack(targetEntity);
            YolBi.instance.getEventManager().post(attackEvent);
            if (attackEvent.isCancelled()) {
                return;
            }
            boolean ignored = attackEvent.isCancelled();//占位

        }

    }

}
