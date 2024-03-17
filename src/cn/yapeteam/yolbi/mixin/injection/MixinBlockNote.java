package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.mixin.annotations.Inject;
import cn.yapeteam.loader.mixin.annotations.Local;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Target;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.block.EventNote;
import net.minecraft.block.BlockNote;
import net.minecraft.util.BlockPos;

@Mixin(BlockNote.class)
public class MixinBlockNote {
    @Inject(
            method = "onBlockEventReceived",
            desc = "(Lnet/minecraft/world/World;Lnet/minecraft/util/BlockPos;Lnet/minecraft/block/state/IBlockState;II)Z",
            target = @Target("HEAD")
    )
    public void onNote(@Local(source = "pos", index = 2) BlockPos pos, @Local(source = "eventParam", index = 5) int eventParam, @Local(source = "eventID", index = 4) int eventID) {
        YolBi.instance.getEventManager().post(new EventNote(pos, eventParam, eventID));
    }
}
