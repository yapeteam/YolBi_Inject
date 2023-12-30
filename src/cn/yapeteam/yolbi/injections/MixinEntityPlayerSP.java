package cn.yapeteam.yolbi.injections;

import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Inject;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Local;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Mixin;
import cn.yapeteam.yolbi.a_pretoload.mixin.annotations.Target;
import cn.yapeteam.yolbi.event.impl.player.EventChat;

@Mixin("net.minecraft.client.entity.EntityPlayerSP")
public class MixinEntityPlayerSP {
    @Inject(method = "sendChatMessage", desc = "(Ljava/lang/String;)V", hasReturn = false, target = @Target("HEAD"))
    public void onSendChatMessage(@Local(source = "message", index = 1) String message) {
        EventChat event = new EventChat(message);
        YolBi.instance.getEventManager().post(event);
        if (event.isCancelled()) return;
        message = event.getMessage();
        boolean ignored = message.isEmpty();
    }
}
