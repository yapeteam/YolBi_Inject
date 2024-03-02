package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Overwrite;
import cn.yapeteam.loader.mixin.annotations.Shadow;
import cn.yapeteam.yolbi.utils.misc.ObjectStore;
import net.minecraft.entity.player.EntityPlayer;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {
    @Shadow
    public boolean isInvisible() {
        return false;
    }

    @Overwrite(
            method = "isInvisibleToPlayer",
            desc = "(Lnet/minecraft/entity/player/EntityPlayer;)Z"
    )
    public boolean isInvisibleToPlayer(EntityPlayer player) {
        Object value = ObjectStore.objects.get("AntiInvisible");
        if (value != null) return !(boolean) value;
        return isInvisible();
    }
}
