package cn.yapeteam.yolbi.injections;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Inject;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Local;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Mixin;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Target;
import cn.yapeteam.yolbi.event.impl.player.EventChat;
import cn.yapeteam.yolbi.event.impl.player.EventUpdate;

@Mixin("net.minecraft.client.entity.EntityPlayerSP")
public class MixinEntityPlayerSP {
    @Inject(
            method = "onUpdate", desc = "()V",
            hasReturn = false,
            target = @Target(
                    value = "INVOKEVIRTUAL",
                    target = "net/minecraft/client/entity/EntityPlayerSP.isRiding()Z",
                    shift = Target.Shift.BEFORE
            )
    )
    public void onUpdate() {
        YolBi.instance.getEventManager().post(new EventUpdate());
    }

    @Inject(method = "sendChatMessage", desc = "(Ljava/lang/String;)V", hasReturn = false, target = @Target("HEAD"))
    public void onSendChatMessage(@Local(source = "message", index = 1) String message) {
        EventChat event = new EventChat(message);
        YolBi.instance.getEventManager().post(event);
        if (event.isCancelled()) return;
        message = event.getMessage();
        boolean ignored = message.isEmpty();//占位
    }
}
