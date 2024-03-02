package cn.yapeteam.yolbi.mixin.injection;

import cn.yapeteam.loader.mixin.annotations.Inject;
import cn.yapeteam.loader.mixin.annotations.Local;
import cn.yapeteam.loader.mixin.annotations.Mixin;
import cn.yapeteam.loader.mixin.annotations.Target;
import cn.yapeteam.yolbi.YolBi;
import cn.yapeteam.yolbi.event.impl.network.EventPacketReceive;
import cn.yapeteam.yolbi.event.impl.network.EventPacketSend;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

@Mixin(NetworkManager.class)
@SuppressWarnings({"UnusedAssignment", "UnnecessaryReturnStatement"})
public class MixinNetworkManager {
    @Inject(
            method = "sendPacket",
            desc = "(Lnet/minecraft/network/Packet;)V",
            target = @Target(
                    value = "INVOKESPECIAL",
                    target = "net/minecraft/network/NetworkManager.flushOutboundQueue()V",
                    shift = Target.Shift.BEFORE
            )
    )
    public void onPacketSend(@Local(source = "packet", index = 1) Packet packet) {
        EventPacketSend eventPacketSend = new EventPacketSend(packet);
        YolBi.instance.getEventManager().post(eventPacketSend);
        packet = eventPacketSend.getPacket();
        if (eventPacketSend.isCancelled()) return;
    }

    @Inject(
            method = "channelRead0",
            desc = "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V",
            target = @Target(
                    value = "INVOKEINTERFACE",
                    target = "net/minecraft/network/Packet.processPacket(Lnet/minecraft/network/INetHandler;)V",
                    shift = Target.Shift.BEFORE
            )
    )
    public void onPacketReceive(@Local(source = "packet", index = 2) Packet packet) {
        EventPacketReceive event = new EventPacketReceive(packet);
        YolBi.instance.getEventManager().post(event);
        if (event.isCancelled()) return;
    }
}
